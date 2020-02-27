package eu.driver.admin.service.kafka;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.avro.generic.IndexedRecord;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

import eu.driver.adapter.properties.ClientProperties;

public class KafkaAdminController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private String schemaRest = "http://localhost:3502";
	private String bootstrapServer = "localhost:9092";
	private String clientId = "TB-AdminTool";
	
	private boolean simulate = false;
	
	public KafkaAdminController() {
		log.info("KafkaAdminController");
		schemaRest = ClientProperties.getInstance().getProperty("schema.registry.url", "http://localhost:3502");
		if (System.getenv().get("SCHEMA_REGISTRY_URL") != null) {
			schemaRest = System.getenv().get("SCHEMA_REGISTRY_URL");
		}
		if (System.getenv().get("KAFKA_BROKER_URL") != null) {
			bootstrapServer = System.getenv().get("KAFKA_BROKER_URL");
		} else if (System.getProperty("KAFKA_BROKER_URL") != null) {
			bootstrapServer = System.getProperty("KAFKA_BROKER_URL");
		}
		
		simulate = Boolean.parseBoolean(ClientProperties.getInstance().getProperty("zookeeper.simulate.creation", "false"));
	}
	
	public void removeTopic(String topicName) throws Exception {
		log.info("--> removeTopic: " + topicName);
		if (simulate) {
        	return;
        }
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(buildDefaultClientConfig());
	
			List<String> newTopics = new ArrayList<String>();
			newTopics.add(topicName);
	
			adminClient.deleteTopics(newTopics);
		} catch (Exception ex) {
        	log.error("Error deleting the topic: " + topicName + "!", ex);
            throw new Exception("Error deleting the topic: " + topicName + "!", ex);
        } finally {
            if (adminClient != null) {
            	adminClient.close();
            }
        }
		log.info("removeTopic -->");
	}
	
	public void createTopic(String topicName, IndexedRecord key, IndexedRecord value, Long retMSTime, int noOfPartitions) throws Exception {
		log.info("--> createTopic: " + topicName);
		if (simulate) {
        	return;
        }
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(buildDefaultClientConfig());
			NewTopic newTopic = new NewTopic(topicName, noOfPartitions, (short)1);
			List<NewTopic> newTopics = new ArrayList<NewTopic>();
			newTopics.add(newTopic);
	
			Set<String> topics = adminClient.listTopics().names().get();
			if (!topics.contains(topicName)) {
				log.info("creating the topic: " + topicName);
				final CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopics);
				createTopicsResult.values().get(topicName).get();
			}
			log.info("Register the schema on that topic!");
        	CachedSchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(schemaRest, AbstractKafkaAvroSerDeConfig.MAX_SCHEMAS_PER_SUBJECT_DEFAULT);
        	schemaRegistryClient.register(topicName + "-key", key.getSchema());
        	schemaRegistryClient.register(topicName + "-value", value.getSchema());
        	log.info("Topic created!");
		} catch (Exception ex) {
        	log.error("Error deleting the topic: " + topicName + "!", ex);
            throw new Exception("Error deleting the topic: " + topicName + "!", ex);
        } finally {
            if (adminClient != null) {
            	adminClient.close();
            }
        }
        log.info("createTopic -->");
    }
	
	private Map<String, Object> buildDefaultClientConfig() {
        Map<String, Object> defaultClientConfig = Maps.newHashMap();
        defaultClientConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        defaultClientConfig.put(AdminClientConfig.RETRIES_CONFIG, 5);
        //defaultClientConfig.put("client.id", clientId);
        return defaultClientConfig;
    }
}
