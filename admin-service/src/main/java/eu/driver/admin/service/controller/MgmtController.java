package eu.driver.admin.service.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.avro.specific.SpecificRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.AdminAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.kafka.KafkaAdminController;
import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSTopicCreationNotification;
import eu.driver.model.cap.Alert;
import eu.driver.model.core.AdminHeartbeat;
import eu.driver.model.core.Heartbeat;
import eu.driver.model.core.Timing;
import eu.driver.model.edxl.EDXLDistribution;
import eu.driver.model.emsi.TSO_2_0;
import eu.driver.model.geojson.FeatureCollection;
import eu.driver.model.mlp.SlRep;
import eu.driver.model.core.Log;
import eu.driver.model.core.TopicCreate;
import eu.driver.model.core.TopicInvite;

@RestController
public class MgmtController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private KafkaAdminController adminController = new KafkaAdminController();
	private AdminAdapter adminAdapter = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	TopicRESTController topicController;
	
	@Autowired
	SolutionRESTController solutionController;
	
	private Boolean initDone = false;
	private Boolean startDone = false;
	
	public MgmtController() {

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
		Boolean send = true;
		
		try {
			createTrialTopics();
			startDone = true;
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
	
	private void createAllCoreTopics() throws Exception {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Creating core Topics!", true);
		adminController.createTopic(TopicConstants.ADMIN_HEARTBEAT_TOPIC, new EDXLDistribution(), new AdminHeartbeat());
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.ADMIN_HEARTBEAT_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.ADMIN_HEARTBEAT_TOPIC, true);
		sendTopicStateChange("core.topic.admin.hb", true);
		
		adminController.createTopic(TopicConstants.HEARTBEAT_TOPIC, new EDXLDistribution(), new Heartbeat());
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.HEARTBEAT_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.HEARTBEAT_TOPIC, true);
		sendTopicStateChange("core.topic.hb", true);
		
		adminController.createTopic(TopicConstants.LOGGING_TOPIC, new EDXLDistribution(), new Log());
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.LOGGING_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.LOGGING_TOPIC, true);
		sendTopicStateChange("core.topic.log", true);
		
		adminController.createTopic(TopicConstants.TIMING_TOPIC, new EDXLDistribution(), new Timing());
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TIMING_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TIMING_TOPIC, true);
		sendTopicStateChange("core.topic.time", true);
		
		adminController.createTopic(TopicConstants.TOPIC_INVITE_TOPIC, new EDXLDistribution(), new TopicInvite());
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TOPIC_INVITE_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TOPIC_INVITE_TOPIC, true);
		sendTopicStateChange("core.topic.access.invite", true);
		
		adminController.createTopic(TopicConstants.TOPIC_CREATE_REQUEST_TOPIC, new EDXLDistribution(), new TopicCreate());
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + TopicConstants.TOPIC_CREATE_REQUEST_TOPIC + " created.", true);
		topicController.updateTopicState(TopicConstants.TOPIC_CREATE_REQUEST_TOPIC, true);
		sendTopicStateChange("core.topic.create.request", true);
		
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
			} else if (topic.getMsgType().equalsIgnoreCase("mlp")) {
				schema = new SlRep();
			} else if (topic.getMsgType().equalsIgnoreCase("emsi")) {
				schema = new TSO_2_0();
			}
			
			if (schema != null) {
				adminController.createTopic(topic.getName(), new EDXLDistribution(), schema);
				logController.addLog(LogLevels.LOG_LEVEL_INFO, "Topic: " + topic.getName() + " created.", true);
				topicController.updateTopicState(topic.getName(), true);
				sendTopicStateChange(topic.getId(), true);
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
							inviteMsg.setId(solution.getId());
							inviteMsg.setTopicName(topic.getName());
							inviteMsg.setPublishAllowed(true);
							inviteMsg.setSubscribeAllowed(true);
							
							inviteMsgs.add(inviteMsg);
						}
					}
				} else if (allSolutionsPublish) {
					for (Solution solution: solutionList) {
						if (!solution.getIsAdmin()) {
							TopicInvite inviteMsg = new TopicInvite();
							inviteMsg.setId(solution.getId());
							inviteMsg.setTopicName(topic.getName());
							inviteMsg.setPublishAllowed(true);
							
							// find the client ID in the list of subribers
							for (String clientID : subscribeClientIDs) {
								if (clientID.equalsIgnoreCase(solution.getId())) {
									inviteMsg.setSubscribeAllowed(true);
									return;	
								}
							}
							
							inviteMsgs.add(inviteMsg);
						}
					}
				} else if (allSolutionsSubscribe) {
					for (Solution solution: solutionList) {
						if (!solution.getIsAdmin()) {
							TopicInvite inviteMsg = new TopicInvite();
							inviteMsg.setId(solution.getId());
							inviteMsg.setTopicName(topic.getName());
							inviteMsg.setSubscribeAllowed(true);
							// find the client ID in the list of subribers
							for (String clientID : publishClientIDs) {
								if (clientID.equalsIgnoreCase(solution.getId())) {
									inviteMsg.setPublishAllowed(true);
									return;	
								}
							}
							inviteMsgs.add(inviteMsg);
						}
					}
				} else {
					Map<String, List<Boolean>> solutionMap = new HashMap<String, List<Boolean>>();
					for (String clientID : publishClientIDs) {
						List<Boolean> flagList = new ArrayList<Boolean>();
						flagList.add(true);
						solutionMap.put(clientID, flagList);
					}
					
					for (String clientID : subscribeClientIDs) {
						List<Boolean> flagList = solutionMap.get(clientID);
						if (flagList == null) {
							flagList = new ArrayList<Boolean>();
							flagList.add(false);
						}
						flagList.add(true);
						solutionMap.put(clientID, flagList);
					}
					
					for (Map.Entry<String, List<Boolean>> entry : solutionMap.entrySet())
					{
						List<Boolean> flagList = entry.getValue();
						TopicInvite inviteMsg = new TopicInvite();
						inviteMsg.setId(entry.getKey());
						inviteMsg.setTopicName(topic.getName());
						inviteMsg.setPublishAllowed(flagList.get(0));
						inviteMsg.setSubscribeAllowed(flagList.get(1));
						inviteMsgs.add(inviteMsg);
					}
				}
				for (TopicInvite inviteMsg : inviteMsgs) {
					try {
						logController.addLog("INFO", "Send Topic InviteMsg: " + inviteMsg, true);
						AdminAdapter.getInstance().sendTopicInviteMessage(inviteMsg);
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
}
