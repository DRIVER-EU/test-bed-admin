package eu.driver.admin.service.kafka;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import java.util.Properties;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.log.LogConfig;
import kafka.utils.ZKStringSerializer;
import kafka.utils.ZkUtils;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.avro.generic.IndexedRecord;
import org.apache.log4j.Logger;

import eu.driver.adapter.properties.ClientProperties;

public class KafkaAdminController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private String schemaRest = "http://localhost:3502";
	private String zookeeperHost = "localhost";
	private int zookeeperPort = 3500;
	
	public KafkaAdminController() {
		log.info("KafkaAdminController");
		schemaRest = ClientProperties.getInstance().getProperty("schema.registry.url", "http://localhost:3502");
		if (System.getenv().get("schema_registry_url") != null) {
			schemaRest = System.getenv().get("schema_registry_url");
		}
		zookeeperHost = ClientProperties.getInstance().getProperty("zookeeper.host", "localhost");
		if (System.getenv().get("zookeeper_host") != null) {
			zookeeperHost = System.getenv().get("zookeeper_host");
		}
		zookeeperPort = Integer.parseInt(ClientProperties.getInstance().getProperty("zookeeper.port", "3500"));
		if (System.getenv().get("zookeeper_port") != null) {
			zookeeperPort = Integer.parseInt(System.getenv().get("zookeeper_port"));
		}
	}
	
	public void createTopic(String topicName, IndexedRecord key, IndexedRecord value, Long retMSTime) throws Exception {
		log.info("--> createTopic: " + topicName);
		ZkClient zkClient = null;
        ZkUtils zkUtils = null;
        try {
            String zookeeperHosts = zookeeperHost + ":" + zookeeperPort;
            int sessionTimeOutInMs = 15 * 1000; // 15 secs
            int connectionTimeOutInMs = 10 * 1000; // 10 secs

            zkClient = new ZkClient(zookeeperHosts, sessionTimeOutInMs, connectionTimeOutInMs);
            zkClient.setZkSerializer(new ZkSerializer() {
				
				@Override
				public byte[] serialize(Object data) throws ZkMarshallingError {
					return ZKStringSerializer.serialize(data);
				}
				
				@Override
				public Object deserialize(byte[] bytes) throws ZkMarshallingError {
					return ZKStringSerializer.deserialize(bytes);
				}
            });
            zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false);

            int noOfPartitions = 1;
            int noOfReplication = 1;
            Properties topicConfiguration = new Properties();
            if (retMSTime != null) {
            	topicConfiguration.setProperty(LogConfig.RetentionMsProp(), Long.toString(retMSTime));
            }
            
            boolean topicExist = AdminUtils.topicExists(zkUtils, topicName);
            if (!topicExist) {
            	log.info("creating the topic: " + topicName);
            	// create the topic
            	AdminUtils.createTopic(zkUtils, topicName, noOfPartitions, noOfReplication, topicConfiguration, RackAwareMode.Enforced$.MODULE$);
            	// register the schema
            	log.info("Register the schema on that topic!");
            	CachedSchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(schemaRest, AbstractKafkaAvroSerDeConfig.MAX_SCHEMAS_PER_SUBJECT_DEFAULT);
            	schemaRegistryClient.register(topicName + "-key", key.getSchema());
            	schemaRegistryClient.register(topicName + "-value", value.getSchema());
            }
            
            log.info("Topic created!");
            
        } catch (Exception ex) {
        	log.error("Error creating the topic and registering the schema!", ex);
            throw new Exception("Error creating the topic and registering the schema!", ex);
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
        log.info("createTopic -->");
    }


}
