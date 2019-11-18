package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import org.apache.avro.specific.SpecificRecord;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.AdminAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.constants.TestbedSecurityMode;
import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.configuration.TestbedConfig;
import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.dto.organisation.Organisation;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.standard.Standard;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.helper.FileReader;
import eu.driver.admin.service.helper.HTTPUtils;
import eu.driver.admin.service.kafka.KafkaAdminController;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.GatewayRepository;
import eu.driver.admin.service.repository.LogRepository;
import eu.driver.admin.service.repository.OrganisationRepository;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.repository.StandardRepository;
import eu.driver.admin.service.repository.TestbedConfigRepository;
import eu.driver.admin.service.repository.TopicRepository;
import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSTopicCreationNotification;
import eu.driver.model.cap.Alert;
import eu.driver.model.core.AdminHeartbeat;
import eu.driver.model.core.Heartbeat;
import eu.driver.model.core.LargeDataUpdate;
import eu.driver.model.core.Log;
import eu.driver.model.core.MapLayerUpdate;
import eu.driver.model.core.ObserverToolAnswer;
import eu.driver.model.core.PhaseMessage;
import eu.driver.model.core.RequestChangeOfTrialStage;
import eu.driver.model.core.RolePlayerMessage;
import eu.driver.model.core.SessionMgmt;
import eu.driver.model.core.Timing;
import eu.driver.model.core.TimingControl;
import eu.driver.model.core.TopicCreate;
import eu.driver.model.core.TopicInvite;
import eu.driver.model.edxl.EDXLDistribution;
import eu.driver.model.emsi.TSO_2_0;
import eu.driver.model.geojson.FeatureCollection;
import eu.driver.model.geojson.GeoJSONEnvelope;
import eu.driver.model.mlp.SlRep;

@RestController
public class MgmtController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private KafkaAdminController adminController = new KafkaAdminController();
	private AdminAdapter adminAdapter = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	private FileReader fileReader = new FileReader();
	private ClientProperties clientProp = ClientProperties.getInstance();
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private Map<String, List<TopicInvite>> clientInviteMsgMap = new ConcurrentHashMap<String, List<TopicInvite>>();
	private Boolean firstInvites = false;
	
	private HTTPUtils httpUtils = new HTTPUtils();
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	TopicRESTController topicController;
	
	@Autowired
	OrganisationRESTController organisationController;
	
	@Autowired
	SolutionRESTController solutionController;
	
	@Autowired
	GatewayRESTController gatewayController;
	
	@Autowired
	OrganisationRepository organisationRepo;
	
	@Autowired
	SolutionRepository solutionRepo;
	
	@Autowired
	GatewayRepository gatewayRepo;
	
	@Autowired
	TopicRepository topicRepo;
	
	@Autowired
	StandardRepository standardRepo;
	
	@Autowired
	LogRepository logRepo;
	
	@Autowired
	ConfigurationRepository configRepo;
	
	@Autowired
	TestbedConfigRepository testbedConfigRepo;
	
	@PersistenceContext(unitName = "AdminService")
	private EntityManager entityManager;
	
	private Boolean initDone = false;
	private Boolean startDone = false;
	private TestbedSecurityMode secureMode = TestbedSecurityMode.DEVELOP;
	private String testbedActiveConfig = "Local Develop";
	
	private String organisationsJson = "config/organisations.json";
	private String tbSolConfigJson = "config/testbed-solutions.json";
	private String solConfigJson = "config/solutions.json";
	private String tbTopicConfigJson = "config/testbed-topics.json";
	private String topicConfigJson = "config/topics.json";
	private String gwConfigJson = "config/gateways.json";
	private String stConfigJson = "config/standards.json";
	private String configurationsJson = "config/configurations.json";
	
	public MgmtController() {
		if (System.getenv().get("testbed_secure_mode") != null) {
			secureMode = TestbedSecurityMode.valueOf(System.getenv().get("testbed_secure_mode"));
		} else {
			secureMode = TestbedSecurityMode.valueOf(clientProp.getProperty("testbed.secure.mode", "DEVELOP"));
		}
		
		if (System.getenv().get("testbed_default_configuration") != null) {
			testbedActiveConfig = System.getenv().get("testbed_default_configuration");
		} else {
			testbedActiveConfig = clientProp.getProperty("testbed.default.configuration", "Local Develop");
		}
		
		if (solutionController != null) {
			solutionController.mgmtController = this;
		}
	}
	
	@Transactional
	public void loadInitData(Boolean resetDB) {
		log.info("--> loadInitData");
		
		if (resetDB) {
			Query query = null;
			try {
				query = entityManager.createNativeQuery("DELETE FROM admin_service.applied_solutions");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.applied_topics");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.configuration");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.testbedconfig");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.solution");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.organisation");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.publishsolutions");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.subscribedsolutions");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.topic");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.gateway");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.managingtypes");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.version");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.standard");
				query.executeUpdate();
				query = entityManager.createNativeQuery("DELETE FROM admin_service.log");
				query.executeUpdate();
			} catch (Exception e) {
				log.error("Error cleaning the DB!", e);
				logController.addLog(LogLevels.LOG_LEVEL_SEVER, "Error cleaning the DB at startup: " + e.getMessage(), true);
			}
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "CleanUp DB done!", true);
		}
		
		try {
			loadOrganisations();
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "Loading Testbed Services/Solutions!", true);
			loadSolutions(this.tbSolConfigJson);
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "Loading Connected Services/Solutions!", true);
			loadSolutions(this.solConfigJson);
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "Loading System Topics!", true);
			loadTopics(this.tbTopicConfigJson);
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "Loading Standard Topics!", true);
			loadTopics(this.topicConfigJson);
			loadGateways();
			loadStandards();
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "Loading Testbed Configurations!", true);
			loadConfigurations();
			// set default configuration if no config is found
			TestbedConfig tbConfig = testbedConfigRepo.findActiveConfig(true);
			if (tbConfig == null) {
				if (testbedActiveConfig != null && this.secureMode != null) {
					tbConfig = new TestbedConfig();
					tbConfig.setConfigName(testbedActiveConfig);
					tbConfig.setIsActive(true);
					tbConfig.setTestbedMode(this.secureMode.toString());
					testbedConfigRepo.saveAndFlush(tbConfig);	
				}
			}
		} catch (Exception e) {
			log.error("Error initializing the AdminTool Database!", e);
			logController.addLog(LogLevels.LOG_LEVEL_SEVER, "The Testbed wasn't initialized successful: " + e.getMessage(), true);
		}
		
		log.info("loadInitData -->");
	}
	
	@ApiOperation(value = "initTestbed", nickname = "initTestbed")
	@RequestMapping(value = "/AdminService/initTestbed", method = RequestMethod.POST )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> initTestbed() {
		log.info("--> initTestbed");
		Boolean send = true;
		
		if (solutionController != null) {
			solutionController.mgmtController = this;
		}
		
		try {
			createAllCoreTopics();
			adminAdapter = AdminAdapter.getInstance();
			adminAdapter.addCallback(solutionController, TopicConstants.HEARTBEAT_TOPIC);
			adminAdapter.addCallback(logController, TopicConstants.LOGGING_TOPIC);
			initDone = true;
		} catch (Exception e) {
			log.error("Error creating the Core Topics!", e);
			logController.addLog(LogLevels.LOG_LEVEL_SEVER, "The Testbed wasn't initialized successful: " + e.getMessage(), true);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "The Testbed was initialized successful!", true);
		log.info("initTestbed -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "startTrialConfig", nickname = "startTrialConfig")
	@RequestMapping(value = "/AdminService/startTrialConfig", method = RequestMethod.POST )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> startTrialConfig() {
		log.info("--> startTrialConfig");
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Trial start called!", true);
		Boolean send = false;
		
		try {
			createTrialTopics();
		} catch (Exception e) {
			log.error("Error creating the Trial Topics!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("startTrialConfig -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "isTestbedInitialized", nickname = "isTestbedInitialized")
	@RequestMapping(value = "/AdminService/isTestbedInitialized", method = RequestMethod.GET )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> isTestbedInitialized() {
		log.info("--> isTestbedInitialized");
		
		log.info("isTestbedInitialized -->");
	    return new ResponseEntity<Boolean>(initDone, HttpStatus.OK);
	}
	
	@ApiOperation(value = "isTrialStarted", nickname = "isTrialStarted")
	@RequestMapping(value = "/AdminService/isTrialStarted", method = RequestMethod.GET )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> isTrialStarted() {
		log.info("--> isTrialStarted");
		
		log.info("isTrialStarted -->");
	    return new ResponseEntity<Boolean>(startDone, HttpStatus.OK);
	}
	
	@ApiOperation(value = "resetTestbed", nickname = "resetTestbed")
	@RequestMapping(value = "/AdminService/resetTestbed", method = RequestMethod.GET )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> resetTestbed() {
		log.info("--> resetTestbed");
		Boolean cleanDone = false;
		Boolean resetDone = false;
		
		this.initDone = false;
		this.startDone = false;
		
		try {
			solutionRepo.deleteAll();
			solutionRepo.flush();
			
			organisationRepo.deleteAll();
			organisationRepo.flush();
			
			topicRepo.deleteAll();
			topicRepo.flush();
			
			gatewayRepo.deleteAll();
			gatewayRepo.flush();
			
			standardRepo.deleteAll();
			standardRepo.flush();
			
			logRepo.deleteAll();
			logRepo.flush();
			
			configRepo.deleteAll();
			configRepo.flush();
			cleanDone = true;
		} catch (Exception e) {
			log.error("Error cleaning the DB!", e);
			logController.addLog(LogLevels.LOG_LEVEL_SEVER, "Error cleaning the DB for reset: " + e.getMessage(), true);
		}
		
		
		if (cleanDone) {
			loadSolutions(this.tbSolConfigJson);
			loadSolutions(this.solConfigJson);
			loadTopics(this.tbTopicConfigJson);
			loadTopics(this.topicConfigJson);
			loadGateways();
			loadStandards();
			loadConfigurations();
			resetDone = true;
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "The Testbed was re-initialized successful!", true);
		} else {
			logController.addLog(LogLevels.LOG_LEVEL_ERROR, "The Testbed wasn't re-initialized successful!", true);
		}
		
		log.info("resetTestbed -->");
	    return new ResponseEntity<Boolean>(resetDone, HttpStatus.OK);
	}
	
	@ApiOperation(value = "deleteLogReocrds", nickname = "deleteLogReocrds")
	@RequestMapping(value = "/AdminService/deleteLogReocrds", method = RequestMethod.GET )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> deleteLogReocrds() {
		log.info("--> deleteLogReocrds");
		Boolean resetDone = false;
		
		logController.removeAllLogs();
		
		log.info("deleteLogReocrds -->");
	    return new ResponseEntity<Boolean>(resetDone, HttpStatus.OK);
	}
	
	@ApiOperation(value = "createOverviewPicture", nickname = "createOverviewPicture")
	@RequestMapping(value = "/AdminService/createOverviewPicture", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = byte[].class),
			@ApiResponse(code = 400, message = "Bad Request", response = byte[].class),
			@ApiResponse(code = 500, message = "Failure", response = byte[].class) })
	public ResponseEntity<byte[]> createOverviewPicture() {
		log.info("-->createOverviewPicture");
		
		ByteArrayOutputStream bous = new ByteArrayOutputStream();
		
		TestbedConfig testbedConfig = testbedConfigRepo.findActiveConfig(true);
		if (testbedConfig != null) {
			String configName = testbedConfig.getConfigName();
			if (configName != null) {
				Configuration configuration = configRepo.findObjectByName(configName);
				List<Topic> topics = configuration.getTopics();
				List<Solution> solutions = configuration.getSolutions();
				
				String source = "@startuml\n";
				source += "!pragma graphviz_dot jdot\n";
				
				for (Topic topic : topics) {
					source += "node \"Technical infratrucure\" {\n";
					if (topic.getClientId().indexOf("core.topic") < 0) {
						source += "() " + topic.getName().replaceAll("-", "") + "\n";
					}
					source += "}\n";
				}
				
				for (Topic topic : topics) {
					if (topic.getClientId().indexOf("core.topic") < 0) {
						List<String> publishIDs = topic.getPublishSolutionIDs();
						
						
						for(String id: publishIDs) {
							if (id.equalsIgnoreCase("all")) {
								for (Solution solution : solutions) {
									source += "[" + solution.getName().replaceAll("-", "") + "] --> " + topic.getName().replaceAll("-", "") + "\n";	
								}
							} else {
								Solution solution = solutionRepo.findObjectByClientId(id);
								source += "[" + solution.getName().replaceAll("-", "") + "] --> " + topic.getName().replaceAll("-", "") + "\n";	
							}
							
						}
						
						List<String> subscribeIDs = topic.getSubscribedSolutionIDs();
						for(String id: subscribeIDs) {
							if (id.equalsIgnoreCase("all")) {
								for (Solution solution : solutions) {
									source += topic.getName().replaceAll("-", "") + " --> [" + solution.getName().replaceAll("-", "") + "]\n";
								}
							} else {
								Solution solution = solutionRepo.findObjectByClientId(id);
								source += topic.getName().replaceAll("-", "") + " --> [" + solution.getName().replaceAll("-", "") + "]\n";
							}
						}
					}
				}
				
				source += "@enduml";
				SourceStringReader reader = new SourceStringReader(source);
			    try {
					reader.generateImage(bous, new FileFormatOption(FileFormat.SVG));
				} catch (IOException e) {
					log.error("Error creating the overview diagram!");
				}
			    // Return a null string if no generation
			    byte[] media = bous.toByteArray();
			    
			    HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.valueOf("image/svg+xml"));
			    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
				log.info("createOverviewPicture-->");
				return new ResponseEntity<byte[]>(media, headers, HttpStatus.OK);
			}
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("image/svg+xml"));
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		log.info("createOverviewPicture-->");
		return new ResponseEntity<byte[]>("".getBytes(), headers, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "getAllConfigurations", nickname = "getAllConfigurations")
	@RequestMapping(value = "/AdminService/getAllConfigurations", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = List.class),
			@ApiResponse(code = 400, message = "Bad Request", response = List.class),
			@ApiResponse(code = 500, message = "Failure", response = List.class) })
	public ResponseEntity<List<Configuration>> getAllConfigurations() {
		log.info("-->getAllConfigurations");
		List<Configuration> configurations =  null;
		
		configurations = configRepo.findAll();
		
		log.info("getAllConfigurations-->");
		return new ResponseEntity<List<Configuration>>(configurations, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getActTestbedConfig", nickname = "getActTestbedConfig")
	@RequestMapping(value = "/AdminService/getActTestbedConfig", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = TestbedConfig.class),
			@ApiResponse(code = 400, message = "Bad Request", response = TestbedConfig.class),
			@ApiResponse(code = 500, message = "Failure", response = TestbedConfig.class) })
	public ResponseEntity<TestbedConfig> getActTestbedConfig() {
		log.info("-->getActTestbedConfig");
		TestbedConfig configuration = null;
		//ByteArrayOutputStream bous = new ByteArrayOutputStream();
		
		configuration = testbedConfigRepo.findActiveConfig(true);
		
		log.info("getActTestbedConfig-->");
		return new ResponseEntity<TestbedConfig>(configuration, HttpStatus.OK);
	}
	
	@ApiOperation(value = "setActTestbedConfig", nickname = "setActTestbedConfig")
	@RequestMapping(value = "/AdminService/setActTestbedConfig", method = RequestMethod.POST)
	@ApiImplicitParams({ @ApiImplicitParam(name = "configuration", value = "the configuration that should be applied", required = true, dataType = "json", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> setActTestbedConfig(@RequestBody TestbedConfig configuration) {
		log.info("-->setActTestbedConfig");
		
		TestbedConfig oldConfiguration = testbedConfigRepo.findActiveConfig(true);
		if (oldConfiguration != null) {
			oldConfiguration.setIsActive(false);
			testbedConfigRepo.save(oldConfiguration);
		}
		configuration.setIsActive(true);
		testbedConfigRepo.saveAndFlush(configuration);
		if (adminAdapter != null) {
			adminAdapter.closeAdapter();	
		}
		
		// delete all topics and set the testbed init to false
		List<Topic> topicList = topicRepo.findAll();
		for (Topic topic : topicList) {
			try {
				adminController.removeTopic(topic.getName());
				topic.setState(false);
				topicRepo.saveAndFlush(topic);
				sendTopicStateChange(topic.getClientId(), false);
			} catch(Exception e) {
				log.error("Error removing the topic: " + topic.getClientId());
			}
		}
		this.initDone = false;
		
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Testbed setting changed to: " + configuration.getConfigName() + ", " + configuration.getTestbedMode(), true);

		log.info("setActTestbedConfig-->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getAllTestbedModes", nickname = "getAllTestbedModes")
	@RequestMapping(value = "/AdminService/getAllTestbedModes", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = TestbedConfig.class),
			@ApiResponse(code = 400, message = "Bad Request", response = TestbedConfig.class),
			@ApiResponse(code = 500, message = "Failure", response = TestbedConfig.class) })
	public ResponseEntity<List<String>> getAllTestbedModes() {
		log.info("-->getAllTestbedModes");
		
		List<String> testbedModes = new ArrayList<String>();
		testbedModes.add(TestbedSecurityMode.DEVELOP.toString());
		testbedModes.add(TestbedSecurityMode.AUTHENTICATION.toString());
		testbedModes.add(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION.toString());
		
		log.info("getAllTestbedModes-->");
		return new ResponseEntity<List<String>>(testbedModes, HttpStatus.OK);
	}
	
	public void sendTopicInvitesForClient(String clientId) {
		if (firstInvites) {
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "Sending TopicInvites for solution: " + clientId, true);
			List<TopicInvite> invites = clientInviteMsgMap.get(clientId);
			
			if (invites != null) {
				invites.forEach((inviteMsg) -> {
					try {
						logController.addLog("INFO", "Send Topic InviteMsg: " + inviteMsg, true);
						AdminAdapter.getInstance().sendTopicInviteMessage(inviteMsg);
					} catch (Exception e) {
						log.error("Error storing client invites!", e);
					}
				});
			}
		}
	}
	
	private void createAllCoreTopics() throws Exception {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Creating core Topics!", true);
		
		List<Topic> topics = topicController.getAllCoreTopic();
		topics.forEach((topic) -> {
			try {
				if (topic.getMsgType() != null) {
					SpecificRecord schema = null;
					if (topic.getMsgType().equalsIgnoreCase("admin_hb")) {
						schema = new AdminHeartbeat();
					} else if (topic.getMsgType().equalsIgnoreCase("hb")) {
						schema = new Heartbeat();
					} else if (topic.getMsgType().equalsIgnoreCase("log")) {
						schema = new Log();
					} else if (topic.getMsgType().equalsIgnoreCase("topic_invite")) {
						schema = new TopicInvite();
					} else if (topic.getMsgType().equalsIgnoreCase("topic_create_request")) {
						schema = new TopicCreate();
					} else if (topic.getMsgType().equalsIgnoreCase("timing")) {
						schema = new Timing();
					} else if (topic.getMsgType().equalsIgnoreCase("timing_control")) {
						schema = new TimingControl();
					} else if (topic.getMsgType().equalsIgnoreCase("phase_msg")) {
						schema = new PhaseMessage();
					} else if (topic.getMsgType().equalsIgnoreCase("role_player")) {
						schema = new RolePlayerMessage();
					} else if (topic.getMsgType().equalsIgnoreCase("session")) {
						schema = new SessionMgmt();
					} else if (topic.getMsgType().equalsIgnoreCase("ost")) {
						schema = new ObserverToolAnswer();
					} else if (topic.getMsgType().equalsIgnoreCase("trial_stage")) {
						schema = new RequestChangeOfTrialStage();
					}
					
					if (schema != null) {
						adminController.createTopic(topic.getName(), new EDXLDistribution(), schema , 300000L, 1);
						logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + topic.getName() + " created.", true);
						topicController.updateTopicState(topic.getName(), true);
						sendTopicStateChange(topic.getClientId(), true);
						if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
							this.grantCoreTopicGroupAccess(TopicConstants.ADMIN_HEARTBEAT_TOPIC);
						}
					}
				}
			} catch(Exception e) {
				logController.addLog(LogLevels.LOG_LEVEL_ERROR, "Could not create all core topics!", true);
			}
		});
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Core Topics created!", true);
	}
	
	private void createTrialTopics() throws Exception {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Creating Trial specific Topics!", true);
		
		List<Solution> solutionList = solutionController.getAllTrialSolutionList();
		List<Topic> topics = topicController.getAllTrialTopicList();
		
		for (Topic topic : topics) {
			if (!topic.getType().equalsIgnoreCase("core.topic")) {
				int noOfPartitions = 1;
				SpecificRecord schema = null;
				if (topic.getMsgType() != null) {
					if (topic.getMsgType().equalsIgnoreCase("cap")) {
						schema = new Alert();
					} else if (topic.getMsgType().equalsIgnoreCase("geojson")) {
						schema = new FeatureCollection();
					} else if (topic.getMsgType().equalsIgnoreCase("geojson-sim")) {
						schema = new eu.driver.model.geojson.sim.FeatureCollection();
					} else if (topic.getMsgType().equalsIgnoreCase("mlp")) {
						schema = new SlRep();
					} else if (topic.getMsgType().equalsIgnoreCase("emsi")) {
						schema = new TSO_2_0();
					} else if (topic.getMsgType().equalsIgnoreCase("largedata")) {
						schema = new LargeDataUpdate();
						noOfPartitions = 2;
					} else if (topic.getMsgType().equalsIgnoreCase("maplayer")) {
						schema = new MapLayerUpdate();
					} else if (topic.getMsgType().equalsIgnoreCase("named-geojson")) {
						schema = new GeoJSONEnvelope();
					} else if (topic.getMsgType().equalsIgnoreCase("photo-geojson")) {
						schema = new eu.driver.model.geojson.photo.FeatureCollection();
					} else if (topic.getMsgType().equalsIgnoreCase("sim-post")) {
						schema = new eu.driver.model.sim.entity.Post();
					} else if (topic.getMsgType().equalsIgnoreCase("sim-startinject")) {
						schema = new eu.driver.model.sim.request.RequestStartInject();
					}
					
					if (schema != null) {
						adminController.createTopic(topic.getName(), new EDXLDistribution(), schema, null, noOfPartitions);
						logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + topic.getName() + " created.", true);
						topicController.updateTopicState(topic.getName(), true);
						sendTopicStateChange(topic.getClientId(), true);
						// send invite message
						
						boolean allSolutionsPublish = false;
						boolean allSolutionsSubscribe = false;
						
						List<String> publishClientIDs = topic.getPublishSolutionIDs();
						List<String> subscribeClientIDs = topic.getSubscribedSolutionIDs();
						
						if (publishClientIDs.size() > 0) {
							if (publishClientIDs.get(0).equalsIgnoreCase("all")) {
								allSolutionsPublish = true;
							}
						}
						
						if (subscribeClientIDs.size() > 0) {
							if (subscribeClientIDs.get(0).equalsIgnoreCase("all")) {
								allSolutionsSubscribe = true;
							}
						}
						
						List<TopicInvite> inviteMsgs = new ArrayList<TopicInvite>();
	
						if (allSolutionsPublish && allSolutionsSubscribe) {
							for (Solution solution: solutionList) {
								if (!solution.getIsAdmin()) {
									TopicInvite inviteMsg = new TopicInvite();
									inviteMsg.setId(solution.getClientId());
									inviteMsg.setTopicName(topic.getName());
									inviteMsg.setPublishAllowed(true);
									inviteMsg.setSubscribeAllowed(true);
									
									inviteMsgs.add(inviteMsg);
								}
							}
						} else if (allSolutionsPublish && !allSolutionsSubscribe) {
							for (Solution solution: solutionList) {
								if (!solution.getIsAdmin()) {
									TopicInvite inviteMsg = new TopicInvite();
									inviteMsg.setId(solution.getClientId());
									inviteMsg.setTopicName(topic.getName());
									inviteMsg.setPublishAllowed(true);
									inviteMsg.setSubscribeAllowed(false);
									
									// find the client ID in the list of subscribers
									for (String clientID : subscribeClientIDs) {
										if (clientID.equalsIgnoreCase(solution.getClientId())) {
											inviteMsg.setSubscribeAllowed(true);
											return;	
										}
									}
									
									inviteMsgs.add(inviteMsg);
								}
							}
						} else if (!allSolutionsPublish && allSolutionsSubscribe) {
							for (Solution solution: solutionList) {
								if (!solution.getIsAdmin()) {
									TopicInvite inviteMsg = new TopicInvite();
									inviteMsg.setId(solution.getClientId());
									inviteMsg.setTopicName(topic.getName());
									inviteMsg.setSubscribeAllowed(true);
									inviteMsg.setPublishAllowed(false);
									// find the client ID in the list of subscribers
									for (String clientID : publishClientIDs) {
										if (clientID.equalsIgnoreCase(solution.getClientId())) {
											inviteMsg.setPublishAllowed(true);
											return;	
										}
									}
									inviteMsgs.add(inviteMsg);
								}
							}
						} else {
							Map<String, Map<String, Boolean>> solutionMap = new HashMap<String, Map<String, Boolean>>();
							
							for (String clientID : publishClientIDs) {
								Map<String, Boolean> flags = new HashMap<String, Boolean>();
								flags.put("publishAllowed", true);
								flags.put("subscribeAllowed", false);
								solutionMap.put(clientID, flags);
							}
							for (String clientID : subscribeClientIDs) {
								Map<String, Boolean> flags = solutionMap.get(clientID);
								if (flags == null) {
									flags = new HashMap<String, Boolean>();
									flags.put("publishAllowed", false);
									flags.put("subscribeAllowed", true);
									solutionMap.put(clientID, flags);
								} else {
									flags.put("subscribeAllowed", true);
									solutionMap.put(clientID, flags);
								}
							}
							
							for (Map.Entry<String, Map<String, Boolean>> entry : solutionMap.entrySet())
							{
								Map<String, Boolean> flags = entry.getValue();
								TopicInvite inviteMsg = new TopicInvite();
								inviteMsg.setId(entry.getKey());
								inviteMsg.setTopicName(topic.getName());
								inviteMsg.setPublishAllowed(flags.get("publishAllowed"));
								inviteMsg.setSubscribeAllowed(flags.get("subscribeAllowed"));
								inviteMsgs.add(inviteMsg);
							}
						}
						for (TopicInvite inviteMsg : inviteMsgs) {
							try {
								logController.addLog("INFO", "Send Topic InviteMsg: " + inviteMsg, true);
								// grant the access to the topics vie the Security REST API
								boolean sendInvite = true;
								// check if adapter is in secure mode
								if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
									sendInvite = grantTopicAccess(inviteMsg);
								}
								
								if (sendInvite) {
									AdminAdapter.getInstance().sendTopicInviteMessage(inviteMsg);
									try {
										List<TopicInvite> invites = clientInviteMsgMap.get(inviteMsg.getId().toString());
										if (invites == null) {
											invites = new ArrayList<TopicInvite>();
										}
										invites.add(inviteMsg);
										clientInviteMsgMap.put(inviteMsg.getId().toString(), invites);
									} catch (Exception e) {
										log.error("Error storing client invites!", e);
									}
								}
							} catch (CommunicationException cEx) {
								logController.addLog(LogLevels.LOG_LEVEL_ERROR, "Topic invite for topic: " + topic.getName() + " could not be send to client: " + inviteMsg.getId().toString(), true);
							}
						}
						//first invites send
						firstInvites = true;
						
					} else {
						logController.addLog(LogLevels.LOG_LEVEL_ERROR, "Trial specific Topic: " + topic.getName() + " could not be created, unknown schema: " + topic.getMsgType(), true);
					}
				}
			}
		}
		
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Trial specific Topics created!", true);
	}
	
	private void sendTopicStateChange(String id, Boolean state) {
		// send the log message via the ws connection
		WSTopicCreationNotification notifyMsg = new WSTopicCreationNotification(id, state);
		WSController.getInstance().sendMessage(
				mapper.objectToJSONString(notifyMsg));
	}
	
	private void loadSolutions(String path) {
		log.info("--> loadSolutions");
		
		String solutionJson = fileReader.readFile(path);
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
					
					String orgName = jsonobject.getString("orgName");
					if (orgName != null) { 
						Organisation organisation = organisationRepo.findObjectByOrgName(orgName);
						solution.setOrganisation(organisation);
					}
					
					if (orgName != null && solution.getName() != null) {
						solution.setSubjectId(
								"O="+orgName.toUpperCase() 
								+ ",CN=" + solution.getName().toUpperCase().replaceAll(" ", "-"));
					}
					
					if (solution.getClientId().equalsIgnoreCase("TB-AdminTool")) {
						solution.setState(true);
					} else {
						solution.setState(jsonobject.getBoolean("state"));
					}
					solution.setDescription(jsonobject.getString("description"));
					
					Solution dbSolution = this.solutionRepo.findObjectByClientId(solution.getClientId());
					if (dbSolution == null) {
						this.solutionRepo.saveAndFlush(solution);
						log.info("add solution: " + solution.getName());
					} else {
						if (solution.getClientId().equalsIgnoreCase("TB-AdminTool")) {
							solution.setState(true);
						} else {
							dbSolution.setState(false);	
						}
						this.solutionRepo.saveAndFlush(dbSolution);
					}
				} 
			} catch (JSONException e) {
				log.error("Error parsind the JSON solution response", e);
			}
		}
		log.info("loadSolutions -->");
	}
	
	private void loadTopics(String path) {
		log.info("--> loadTopics");
		try {
			String topicJson = fileReader.readFile(path);
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
				
				Topic dbTopic = this.topicRepo.findObjectByClientId(topic.getClientId());
				if (dbTopic == null) {
					this.topicRepo.saveAndFlush(topic);
					log.info("add topic: " + topic.getName());
				} else {
					dbTopic.setState(false);
					this.topicRepo.saveAndFlush(dbTopic);
				}
			}
		} catch (JSONException e) {
			log.error("Error parsind the JSON topic response", e);
		}
		log.info("loadTopics -->");
	}
	
	private void loadOrganisations() {
		log.info("--> loadOrganisations");
		String organisationJson = fileReader.readFile(this.organisationsJson);
		if (organisationJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(organisationJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					Organisation organisation = objectMapper.readValue(jsonarray.getJSONObject(i).toString(), Organisation.class);  
					Organisation dbOrganisation = this.organisationRepo.findObjectByOrgName(organisation.getOrgName());
					if (dbOrganisation == null) {
						dbOrganisation = this.organisationRepo.saveAndFlush(organisation);
						log.info("add organisation: " + dbOrganisation.getOrgName());
					}
				}
			} catch (JSONException | IOException e) {
				log.error("Error parsind the JSON Organisation response", e);
			}
		}
		log.info("loadOrganisations -->");
	}
	
	
	private void loadGateways() {
		log.info("--> loadGateways");
		String gatewayJson = fileReader.readFile(this.gwConfigJson);
		if (gatewayJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(gatewayJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					Gateway gateway = objectMapper.readValue(jsonarray.getJSONObject(i).toString(), Gateway.class);  
					Gateway dbGateway = this.gatewayRepo.findObjectByClientId(gateway.getClientId());
					if (dbGateway == null) {
						this.gatewayRepo.saveAndFlush(gateway);
						log.info("add gateway: " + gateway.getName());
					} else {
						dbGateway.setState(false);
						this.gatewayRepo.saveAndFlush(dbGateway);
					}
				}
			} catch (JSONException | IOException e) {
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
					Standard standard = objectMapper.readValue(jsonarray.getJSONObject(i).toString(), Standard.class);  
					if (this.standardRepo.findObjectByName(standard.getName()) == null) {
						this.standardRepo.saveAndFlush(standard);
						log.info("add standard: " + standard.getName());
					}
				}
			} catch (JSONException | IOException e) {
				log.error("Error parsind the JSON Standard response", e);
			}
		}
		log.info("loadStandards -->");
	}
	
	private void loadConfigurations() {
		log.info("--> loadConfigurations");
		String configurationJson = fileReader.readFile(this.configurationsJson);
		
		if (configurationJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(configurationJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					jsonobject = jsonarray.getJSONObject(i);
					String configName = jsonobject.getString("name");
					Configuration configuration = configRepo.findObjectByName(configName);
					if (configuration == null) {
						configuration = new Configuration();
	
						configuration.setName(configName);
						configuration.setDiscription(jsonobject.getString("discription"));
						configuration = configRepo.saveAndFlush(configuration);
						
						JSONArray jsonSolutions = jsonobject.getJSONArray("solutions");
						List<Solution> solutions = new ArrayList<Solution>();
						if (jsonSolutions != null) { 
						   for (int a=0;a<jsonSolutions.length();a++){ 
							   try {
								   Solution sol = solutionRepo.findObjectByClientId(jsonSolutions.getString(a));
								   if (sol != null) {
									   sol.addApplSolConfigurations(configuration);
									   solutions.add(sol);
								   }
							   } catch (Exception  e) {
								   logController.addLog(LogLevels.LOG_LEVEL_ERROR, "The solution: " + jsonSolutions.getString(a) + " defined in the configuration cannot be found!", true);
							   }
						   } 
						} 
						configuration.setSolutions(solutions);
						
						JSONArray jsonTopics = jsonobject.getJSONArray("topics");
						List<Topic> topics = new ArrayList<Topic>();
						if (jsonTopics != null) { 
						   for (int a=0;a<jsonTopics.length();a++){ 
							   try {
								   Topic top = topicRepo.findObjectByClientId(jsonTopics.getString(a));
								   top.addApplConfigurations(configuration);
								   if (top != null) {
									   topics.add(top);
								   }
							   } catch (Exception  e) {
								   logController.addLog(LogLevels.LOG_LEVEL_ERROR, "The topic: " + jsonTopics.getString(a) + " defined in the configuration cannot be found!", true);
							   }
						   } 
						} 
						configuration.setTopics(topics);
						
						/*JSONArray jsonGateways = jsonobject.getJSONArray("gateways");
						List<Gateway> gateways = new ArrayList<Gateway>();
						if (jsonGateways != null) { 
						   for (int a=0;a<jsonGateways.length();a++){ 
							   try {
								   Gateway gw = gatewayRepo.findObjectByClientId(jsonGateways.getString(a));
								   if (gw != null) {
									   gateways.add(gw);
								   }
							   } catch (Exception  e) {
								   logController.addLog(LogLevels.LOG_LEVEL_ERROR, "The gateway: " + jsonGateways.getString(a) + " defined in the configuration cannot be found!", true);
							   }
						   } 
						} 
						configuration.setGateways(gateways);*/
	
						configRepo.saveAndFlush(configuration);
					} else {
						log.info("The configuration " + configName + " is already available!");
					}
				}
			} catch (JSONException e) {
				log.error("Error parsind the JSON Configuration response", e);
			}
		}
		
		log.info("loadConfigurations -->");
	}
	
	private boolean grantGroupAccess(String clientID, String subjectID, String topicName) {
		log.info("--> grantGroupAccess");
		boolean granted = false;
		if (subjectID == null) {
			subjectID = this.getSubjectIdForClientId(clientID);	
		}
		JSONObject rulesObject = new JSONObject();
		
		try {
			
			JSONObject permissionsObject = new JSONObject();
			
			JSONObject publishObject = new JSONObject();
			publishObject.put("allow", true);
			publishObject.put("action", "READ");
			
			JSONObject describeObject = new JSONObject();
			describeObject.put("allow", true);
			describeObject.put("action", "DESCRIBE");
			
			JSONArray permissions = new JSONArray();
			permissions.put(publishObject);
			permissions.put(describeObject);
			
			permissionsObject.put("permissions", permissions);
			permissionsObject.put("subject.id", subjectID);
			
			JSONArray rules = new JSONArray();
			rules.put(permissionsObject);
			
			rulesObject.put("rules", rules);
			
		} catch (JSONException jEx) {
			log.error("Error creating the JSON access grant structure!");
			return false;
		}
		
		String url = clientProp.getProperty("testbed.admin.security.rest.path.group");
		if (System.getenv().get("security_rest_path_group") != null) {
			url = url.replace("https://localhost:9443", System.getenv().get("security_rest_path_group"));
		}
		url += clientID;
		
		try {
			httpUtils.postHTTPRequest(url, "PUT", "application/json", rulesObject.toString());
			granted = true;
		} catch (CommunicationException cex) {
			log.error("Error grantig the access: " + cex.getMessage());
			return false;
		}
		
		if (granted) {
			rulesObject = new JSONObject();
			
			try {
				
				JSONObject permissionsObject = new JSONObject();
				
				JSONObject publishObject = new JSONObject();
				publishObject.put("allow", true);
				publishObject.put("action", "READ");
				
				JSONObject describeObject = new JSONObject();
				describeObject.put("allow", true);
				describeObject.put("action", "DESCRIBE");
				
				JSONArray permissions = new JSONArray();
				permissions.put(publishObject);
				permissions.put(describeObject);
				
				permissionsObject.put("permissions", permissions);
				permissionsObject.put("subject.group", clientID);
				
				JSONArray rules = new JSONArray();
				rules.put(permissionsObject);
				
				rulesObject.put("rules", rules);
				
			} catch (JSONException jEx) {
				log.error("Error creating the JSON access grant structure!");
			}
			
			url = clientProp.getProperty("testbed.admin.security.rest.path.topic");
			if (System.getenv().get("security_rest_path_topic") != null) {
				url = url.replace("https://localhost:9443", System.getenv().get("security_rest_path_topics"));
			}
			url += topicName;
			
			try {
				httpUtils.postHTTPRequest(url, "PUT", "application/json", rulesObject.toString());
				granted = true;
			} catch (CommunicationException cex) {
				log.error("Error grantig the access: " + cex.getMessage());
			}
		}
		
		log.info("grantGroupAccess -->");
		return granted;
	}
	
	private boolean grantTopicAccess(TopicInvite inviteMessage) {
		log.info("--> grantTopicAccess");
		boolean granted = false;
		
		String clientID = inviteMessage.getId().toString();
		String subjectID = this.getSubjectIdForClientId(clientID);
		String topicName = inviteMessage.getTopicName().toString();
		boolean publishAllowed = inviteMessage.getPublishAllowed();
		boolean subscribeAllowed = inviteMessage.getSubscribeAllowed();
		
		JSONObject rulesObject = new JSONObject();
		
		try {
			
			JSONObject permissionsObject = new JSONObject();
			
			JSONObject publishObject = new JSONObject();
			publishObject.put("allow", publishAllowed);
			publishObject.put("action", "PUBLISH");
			
			JSONObject subsribeObject = new JSONObject();
			subsribeObject.put("allow", subscribeAllowed);
			subsribeObject.put("action", "SUBSCRIBE");
			
			JSONArray permissions = new JSONArray();
			permissions.put(publishObject);
			permissions.put(subsribeObject);
			
			permissionsObject.put("permissions", permissions);
			permissionsObject.put("subject.id", subjectID);
			
			JSONArray rules = new JSONArray();
			rules.put(permissionsObject);
			
			rulesObject.put("rules", rules);
			
		} catch (JSONException jEx) {
			log.error("Error creating the JSON access grant structure!");
		}
		
		String url = clientProp.getProperty("testbed.admin.security.rest.path.topic");
		if (System.getenv().get("security_rest_path_topic") != null) {
			url = url.replace("https://localhost:9443", System.getenv().get("security_rest_path_topics"));
		}
		url += topicName;
		
		try {
			httpUtils.postHTTPRequest(url, "PUT", "application/json", rulesObject.toString());
			granted = true;
			
			if (subscribeAllowed) {
				granted = this.grantGroupAccess(clientID, null, topicName);	
			}
		} catch (CommunicationException cex) {
			log.error("Error grantig the access: " + cex.getMessage());
			granted = false;
		}
		
		log.info("grantTopicAccess -->");
		return granted;
	}
	
	private String getSubjectIdForClientId(String clientId) {
		log.info("--> getSubjectIdForClientId");
		String subjectId = null;
		
		Solution solution = this.solutionRepo.findObjectByClientId(clientId);
		if (solution != null) {
			subjectId = solution.getSubjectId();
		}
		
		log.info("getSubjectIdForClientId -->");
		return subjectId;
	}
	
	private void grantCoreTopicGroupAccess(String topicName) {
		log.info("--> grantCoreTopicGroupAccess");
		
		List<Solution> solutions = this.solutionController.getSolutionList();
		
		for (Solution solution : solutions) {
			this.grantGroupAccess(solution.getClientId(), solution.getSubjectId(), topicName);
		}
		
		log.info("grantCoreTopicGroupAccess -->");
	}

	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}

	public TopicRESTController getTopicController() {
		return topicController;
	}

	public void setTopicController(TopicRESTController topicController) {
		this.topicController = topicController;
	}

	public SolutionRESTController getSolutionController() {
		return solutionController;
	}

	public void setSolutionController(SolutionRESTController solutionController) {
		this.solutionController = solutionController;
	}

	public GatewayRESTController getGatewayController() {
		return gatewayController;
	}

	public void setGatewayController(GatewayRESTController gatewayController) {
		this.gatewayController = gatewayController;
	}
	
	
	
}
