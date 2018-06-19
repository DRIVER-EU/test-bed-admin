package eu.driver.admin.service;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.controller.LogRESTController;
import eu.driver.admin.service.controller.MgmtController;
import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.standard.Standard;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.helper.FileReader;
import eu.driver.admin.service.repository.GatewayRepository;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.repository.StandardRepository;
import eu.driver.admin.service.repository.TopicRepository;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@EnableSwagger2
@SpringBootApplication
public class AdminServiceApplication {

	private Logger log = Logger.getLogger(this.getClass());
	
	private String solConfigJson = "config/solutions.json";
	private String topicConfigJson = "config/topics.json";
	private String gwConfigJson = "config/gateways.json";
	private String stConfigJson = "config/standards.json";
	
	private FileReader fileReader = new FileReader();
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	MgmtController mgmtController;
	
	@Autowired
	SolutionRepository solutionRepo;
	
	@Autowired
	GatewayRepository gatewayRepo;
	
	@Autowired
	TopicRepository topicRepo;
	
	@Autowired
	StandardRepository standardRepo;
	
	public AdminServiceApplication() throws Exception {
		log.info("Init. AdminServiceApplication");
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(AdminServiceApplication.class, args);
    }
	
	@PostConstruct
	public void init() {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "The AdminService is up!", true);
		if(Boolean.parseBoolean(ClientProperties.getInstance().getProperty("init.auto"))) {
			mgmtController.initTestbed();
		}
		
		// add the TestbedService Solutions/Topics/Gateways
		loadSolutions();
		loadTopics();
		loadGateways();
		loadStandards();
	}
	
	@Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("AdminService")
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/AdminService.*"))
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("AdminServiceApplication REST Interface API Doc.")
                .description("This is the AdminServiceApplication REST Interface API Documentation made with Swagger.")
                .version("1.0")
                .build();
    }
	
	private void loadSolutions() {
		log.info("--> loadSolutions");
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Initializing Testbed Services/Solutions!", true);
		String solutionJson = fileReader.readFile(this.solConfigJson);
		if (solutionJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(solutionJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Solution solution = new Solution();
					jsonobject = jsonarray.getJSONObject(i);

					solution.setClientId(jsonobject.getString("id"));
					solution.setName(jsonobject.getString("name"));
					solution.setIsAdmin(jsonobject.getBoolean("isTestbed"));
					solution.setIsService(jsonobject.getBoolean("isService"));
					if (solution.getClientId().equalsIgnoreCase("TB-AdminTool")) {
						solution.setState(true);
					} else {
						solution.setState(jsonobject.getBoolean("state"));
					}
					solution.setDescription(jsonobject.getString("description"));
					
					if (this.solutionRepo.findObjectByClientId(solution.getClientId()) == null) {
						this.solutionRepo.saveAndFlush(solution);
						log.info("add solution: " + solution.getName());
					}
				}
			} catch (JSONException e) {
				log.error("Error parsind the JSON solution response", e);
			}
		}
		log.info("loadSolutions -->");
	}
	
	private void loadTopics() {
		log.info("--> loadTopics");
		try {
			String topicJson = fileReader.readFile(this.topicConfigJson);
			JSONArray jsonarray = new JSONArray(topicJson);
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject jsonobject;
				Topic topic = new Topic();
				jsonobject = jsonarray.getJSONObject(i);

				topic.setClientId(jsonobject.getString("id"));
				topic.setType(jsonobject.getString("type"));
				topic.setName(jsonobject.getString("name"));
				
				if (jsonobject.has("msgType")) {
					topic.setMsgType(jsonobject.getString("msgType"));
					topic.setMsgTypeVersion(jsonobject.getString("msgTypeVersion"));
				}
				topic.setState(jsonobject.getBoolean("state"));
				topic.setDescription(jsonobject.getString("description"));

				ArrayList<String> publisher = new ArrayList<String>();     
				JSONArray jArray = jsonobject.getJSONArray("publishSolutionIDs");
				if (jArray != null) { 
				   for (int a=0;a<jArray.length();a++){ 
					   publisher.add(jArray.getString(a));
				   } 
				} 
				topic.setPublishSolutionIDs(publisher);
				
				ArrayList<String> subscriber = new ArrayList<String>();     
				jArray = jsonobject.getJSONArray("subscribedSolutionIDs");
				if (jArray != null) { 
				   for (int a=0;a<jArray.length();a++){ 
					   subscriber.add(jArray.getString(a));
				   } 
				} 
				topic.setSubscribedSolutionIDs(subscriber);
				
				if (this.topicRepo.findObjectByClientId(topic.getClientId()) == null) {
					this.topicRepo.saveAndFlush(topic);
					log.info("add topic: " + topic.getName());
				}
			}
		} catch (JSONException e) {
			log.error("Error parsind the JSON topic response", e);
		}
		log.info("loadTopics -->");
	}
	
	private void loadGateways() {
		log.info("--> loadGateways");
		String gatewayJson = fileReader.readFile(this.gwConfigJson);
		if (gatewayJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(gatewayJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Gateway gateway = new Gateway();
					jsonobject = jsonarray.getJSONObject(i);

					gateway.setClientId(jsonobject.getString("id"));
					gateway.setName(jsonobject.getString("name"));
					gateway.setState(jsonobject.getBoolean("state"));
					gateway.setDescription(jsonobject.getString("description"));

					ArrayList<String> mangTypes = new ArrayList<String>();     
					JSONArray jArray = jsonobject.getJSONArray("managingTypes");
					if (jArray != null) { 
					   for (int a=0;a<jArray.length();a++){ 
						   mangTypes.add(jArray.getString(a));
					   } 
					} 
					gateway.setManagingType(mangTypes);
					
					if (this.gatewayRepo.findObjectByClientId(gateway.getClientId()) == null) {
						this.gatewayRepo.saveAndFlush(gateway);
						log.info("add gateway: " + gateway.getName());
					}
				}
			} catch (JSONException e) {
				log.error("Error parsind the JSON Gateway response", e);
			}
		}
		log.info("loadGateways -->");
	}
	
	private void loadStandards() {
		log.info("--> loadStandards");
		String standardJson = fileReader.readFile(this.stConfigJson);
		if (standardJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(standardJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Standard standard = new Standard();
					jsonobject = jsonarray.getJSONObject(i);

					standard.setName(jsonobject.getString("name"));

					ArrayList<String> versions = new ArrayList<String>();     
					JSONArray jArray = jsonobject.getJSONArray("versions");
					if (jArray != null) { 
					   for (int a=0;a<jArray.length();a++){ 
						   versions.add(jArray.getString(a));
					   } 
					} 
					standard.setVersions(versions);
					
					if (this.standardRepo.findObjectByName(standard.getName()) == null) {
						this.standardRepo.saveAndFlush(standard);
						log.info("add standard: " + standard.getName());
					}
				}
			} catch (JSONException e) {
				log.error("Error parsind the JSON Standard response", e);
			}
		}
		log.info("loadStandards -->");
	}

	public MgmtController getMgmtController() {
		return mgmtController;
	}

	public void setMgmtController(MgmtController mgmtController) {
		this.mgmtController = mgmtController;
	}

	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}

	public SolutionRepository getSolutionRepo() {
		return solutionRepo;
	}

	public void setSolutionRepo(SolutionRepository solutionRepo) {
		this.solutionRepo = solutionRepo;
	}

	public GatewayRepository getGatewayRepo() {
		return gatewayRepo;
	}

	public void setGatewayRepo(GatewayRepository gatewayRepo) {
		this.gatewayRepo = gatewayRepo;
	}

	public TopicRepository getTopicRepo() {
		return topicRepo;
	}

	public void setTopicRepo(TopicRepository topicRepo) {
		this.topicRepo = topicRepo;
	}

	public StandardRepository getStandardRepo() {
		return standardRepo;
	}

	public void setStandardRepo(StandardRepository standardRepo) {
		this.standardRepo = standardRepo;
	}
}
