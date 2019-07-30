package eu.driver.admin.service.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.avro.specific.SpecificRecord;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.AdminAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.constants.TestbedSecurityMode;
import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.configuration.TestbedConfig;
import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.standard.Standard;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.helper.FileReader;
import eu.driver.admin.service.helper.HTTPUtils;
import eu.driver.admin.service.kafka.KafkaAdminController;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.GatewayRepository;
import eu.driver.admin.service.repository.LogRepository;
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
import eu.driver.model.core.MapLayerUpdate;
import eu.driver.model.core.ObserverToolAnswer;
import eu.driver.model.core.RequestChangeOfTrialStage;
import eu.driver.model.core.RolePlayerMessage;
import eu.driver.model.core.Timing;
import eu.driver.model.core.TimingControl;
import eu.driver.model.edxl.EDXLDistribution;
import eu.driver.model.emsi.TSO_2_0;
import eu.driver.model.geojson.FeatureCollection;
import eu.driver.model.geojson.GeoJSONEnvelope;
import eu.driver.model.mlp.SlRep;
import eu.driver.model.core.PhaseMessage;
import eu.driver.model.core.SessionMgmt;
import eu.driver.model.core.Log;
import eu.driver.model.core.TopicCreate;
import eu.driver.model.core.TopicInvite;

@RestController
public class MgmtController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private KafkaAdminController adminController = new KafkaAdminController();
	private AdminAdapter adminAdapter = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	private FileReader fileReader = new FileReader();
	private ClientProperties clientProp = ClientProperties.getInstance();
	
	private HTTPUtils httpUtils = new HTTPUtils();
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	TopicRESTController topicController;
	
	@Autowired
	SolutionRESTController solutionController;
	
	@Autowired
	GatewayRESTController gatewayController;
	
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
	
	private Boolean initDone = false;
	private Boolean startDone = false;
	private TestbedSecurityMode secureMode = TestbedSecurityMode.DEVELOP;
	
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
	}
	
	public void loadInitData(Boolean resetDB) {
		log.info("--> loadInitData");
		
		if (resetDB) {
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "CleanUp the DB!", true);
			try {
				solutionRepo.deleteAll();
				topicRepo.deleteAll();
				gatewayRepo.deleteAll();
				standardRepo.deleteAll();	
				logRepo.deleteAll();
				configRepo.deleteAll();
			} catch (Exception e) {
				log.error("Error cleaning the DB!", e);
				logController.addLog(LogLevels.LOG_LEVEL_SEVER, "Error cleaning the DB at startup: " + e.getMessage(), true);
			}
		}
		
		try {
			loadSolutions(this.tbSolConfigJson);
			loadSolutions(this.solConfigJson);
			loadTopics(this.tbTopicConfigJson);
			loadTopics(this.topicConfigJson);
			loadGateways();
			loadStandards();
			loadConfigurations();
		} catch (Exception e) {
			log.error("Error initializing the AdminTool Database!", e);
			logController.addLog(LogLevels.LOG_LEVEL_SEVER, "The Testbed wasn't initialized successful: " + e.getMessage(), true);
		}
		
		log.info("loadInitData -->");
	}
	
	@SuppressWarnings("static-access")
	@ApiOperation(value = "initTestbed", nickname = "initTestbed")
	@RequestMapping(value = "/AdminService/initTestbed", method = RequestMethod.POST )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> initTestbed() {
		log.info("--> initTestbed");
		Boolean send = true;
		
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
			topicRepo.deleteAll();
			gatewayRepo.deleteAll();
			standardRepo.deleteAll();	
			logRepo.deleteAll();
			configRepo.deleteAll();
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
		//ByteArrayOutputStream bous = new ByteArrayOutputStream();
		
		log.info("createOverviewPicture-->");
		return new ResponseEntity<byte[]>("".getBytes(), HttpStatus.OK);
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
		
		testbedConfigRepo.saveAndFlush(configuration);

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
	
	private void createAllCoreTopics() throws Exception {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Creating core Topics!", true);
		
		adminController.createTopic(TopicConstants.ADMIN_HEARTBEAT_TOPIC, new EDXLDistribution(), new AdminHeartbeat(), 300000L);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.ADMIN_HEARTBEAT_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.ADMIN_HEARTBEAT_TOPIC, true);
		sendTopicStateChange("core.topic.admin.hb", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.ADMIN_HEARTBEAT_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.HEARTBEAT_TOPIC, new EDXLDistribution(), new Heartbeat(), 300000L);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.HEARTBEAT_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.HEARTBEAT_TOPIC, true);
		sendTopicStateChange("core.topic.hb", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.HEARTBEAT_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.LOGGING_TOPIC, new EDXLDistribution(), new Log(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.LOGGING_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.LOGGING_TOPIC, true);
		sendTopicStateChange("core.topic.log", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.LOGGING_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.TIMING_TOPIC, new EDXLDistribution(), new Timing(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TIMING_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TIMING_TOPIC, true);
		sendTopicStateChange("core.topic.time", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.TIMING_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.TIMING_CONTROL_TOPIC, new EDXLDistribution(), new TimingControl(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TIMING_CONTROL_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TIMING_CONTROL_TOPIC, true);
		sendTopicStateChange("core.topic.time.control", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.TIMING_CONTROL_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.TOPIC_INVITE_TOPIC, new EDXLDistribution(), new TopicInvite(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TOPIC_INVITE_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TOPIC_INVITE_TOPIC, true);
		sendTopicStateChange("core.topic.access.invite", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.TOPIC_INVITE_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.TOPIC_CREATE_REQUEST_TOPIC, new EDXLDistribution(), new TopicCreate(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TOPIC_CREATE_REQUEST_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TOPIC_CREATE_REQUEST_TOPIC, true);
		sendTopicStateChange("core.topic.create.request", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.TOPIC_CREATE_REQUEST_TOPIC);
		}
				
		adminController.createTopic(TopicConstants.TRIAL_STATE_CHANGE_TOPIC, new EDXLDistribution(), new RequestChangeOfTrialStage(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TRIAL_STATE_CHANGE_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TRIAL_STATE_CHANGE_TOPIC, true);
		sendTopicStateChange("core.topic.trial.stage.change", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.TRIAL_STATE_CHANGE_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.OST_ANSWER_TOPIC, new EDXLDistribution(), new ObserverToolAnswer(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.OST_ANSWER_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.OST_ANSWER_TOPIC, true);
		sendTopicStateChange("core.topic.ost.answer", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.OST_ANSWER_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.PHASE_MESSAGE_TOPIC, new EDXLDistribution(), new PhaseMessage(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.PHASE_MESSAGE_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.PHASE_MESSAGE_TOPIC, true);
		sendTopicStateChange("core.topic.tm.phasemessage", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.PHASE_MESSAGE_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.ROLE_PLAYER_TOPIC, new EDXLDistribution(), new RolePlayerMessage(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.ROLE_PLAYER_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.ROLE_PLAYER_TOPIC, true);
		sendTopicStateChange("core.topic.tm.roleplayer", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.ROLE_PLAYER_TOPIC);
		}
		
		adminController.createTopic(TopicConstants.SESSION_MGMT_TOPIC, new EDXLDistribution(), new SessionMgmt(), null);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.SESSION_MGMT_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.SESSION_MGMT_TOPIC, true);
		sendTopicStateChange("core.topic.tm.sessionmgmt", true);
		if (secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) {
			this.grantCoreTopicGroupAccess(TopicConstants.SESSION_MGMT_TOPIC);
		}
		
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Core Topics created!", true);
	}
	
	private void createTrialTopics() throws Exception {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Creating Trial specific Topics!", true);
		
		List<Solution> solutionList = solutionController.getSolutionList();
		List<Topic> topics = topicController.getAllTrialTopic();
		
		for (Topic topic : topics) {
			SpecificRecord schema = null;
			
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
			} else if (topic.getMsgType().equalsIgnoreCase("maplayer")) {
				schema = new MapLayerUpdate();
			} else if (topic.getMsgType().equalsIgnoreCase("named-geojson")) {
				schema = new GeoJSONEnvelope();
			} else if (topic.getMsgType().equalsIgnoreCase("photo-geojson")) {
				schema = new eu.driver.model.geojson.photo.FeatureCollection();
			}
			
			if (schema != null) {
				adminController.createTopic(topic.getName(), new EDXLDistribution(), schema, null);
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
						}
					} catch (CommunicationException cEx) {
						logController.addLog(LogLevels.LOG_LEVEL_ERROR, "Topic invite for topic: " + topic.getName() + " could not be send to client: " + inviteMsg.getId().toString(), true);
					}
				}
			} else {
				logController.addLog(LogLevels.LOG_LEVEL_ERROR, "Trial specific Topic: " + topic.getName() + " could not be created, unknown schema: " + topic.getMsgType(), true);
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
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Loading Testbed Services/Solutions!", true);
		String solutionJson = fileReader.readFile(path);
		if (solutionJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(solutionJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Solution solution = new Solution();
					jsonobject = jsonarray.getJSONObject(i);

					solution.setClientId(jsonobject.getString("id"));
					solution.setSubjectId(jsonobject.getString("subject.id"));
					solution.setName(jsonobject.getString("name"));
					solution.setIsAdmin(jsonobject.getBoolean("isTestbed"));
					solution.setIsService(jsonobject.getBoolean("isService"));
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
					
					Gateway dbGateway = this.gatewayRepo.findObjectByClientId(gateway.getClientId());
					if (dbGateway == null) {
						this.gatewayRepo.saveAndFlush(gateway);
						log.info("add gateway: " + gateway.getName());
					} else {
						dbGateway.setState(false);
						this.gatewayRepo.saveAndFlush(dbGateway);
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
	
	private void loadConfigurations() {
		log.info("--> loadConfigurations");
		String configurationJson = fileReader.readFile(this.configurationsJson);
		
		if (configurationJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(configurationJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Configuration configuration = new Configuration();
					jsonobject = jsonarray.getJSONObject(i);

					configuration.setName(jsonobject.getString("name"));
					configuration.setDiscription(jsonobject.getString("discription"));
					JSONArray jsonSolutions = jsonobject.getJSONArray("solutions");
					List<Solution> solutions = new ArrayList<Solution>();
					if (jsonSolutions != null) { 
					   for (int a=0;a<jsonSolutions.length();a++){ 
						   try {
							   Solution sol = solutionRepo.findObjectByClientId(jsonSolutions.getString(a));
							   if (sol != null) {
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
							   if (top != null) {
								   topics.add(top);
							   }
						   } catch (Exception  e) {
							   logController.addLog(LogLevels.LOG_LEVEL_ERROR, "The topic: " + jsonTopics.getString(a) + " defined in the configuration cannot be found!", true);
						   }
					   } 
					} 
					configuration.setTopics(topics);
					
					JSONArray jsonGateways = jsonobject.getJSONArray("gateways");
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
					configuration.setGateways(gateways);

					configRepo.saveAndFlush(configuration);
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
