package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.AdminAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.constants.TestbedSecurityMode;
import eu.driver.admin.service.dto.TopicList;
import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.configuration.TestbedConfig;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.standard.Standard;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.repository.StandardRepository;
import eu.driver.admin.service.repository.TestbedConfigRepository;
import eu.driver.admin.service.repository.TopicRepository;
import eu.driver.admin.service.util.TopicInviteUtil;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.TopicInvite;
import eu.driver.model.core.TopicRemove;

@RestController
public class TopicRESTController implements IAdaptorCallback {
	private Logger log = Logger.getLogger(this.getClass());
	
	private Boolean invitesSend = false;
	private TopicInviteUtil topicUtil = new TopicInviteUtil();
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	TopicRepository topicRepo;
	
	@Autowired
	StandardRepository standardRepo;
	
	@Autowired
	ConfigurationRepository configRepo;
	
	@Autowired
	TestbedConfigRepository testbedConfigRepo;
	
	@Autowired
	SolutionRepository solutionRepo;
	
	public MgmtController mgmtController;
	
	

	public TopicRESTController() {
		log.info("--> TopicRESTController");
		
		log.info("TopicRESTController -->");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage, String topicName) {
		log.debug("CreateTopicRequest message received-->");
		
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("TopicCreate")) {
			try {
				eu.driver.model.core.TopicCreate createTopic = (eu.driver.model.core.TopicCreate) SpecificData.get().deepCopy(eu.driver.model.core.TopicCreate.SCHEMA$, receivedMessage);
				String topicNameToCreate = createTopic.getTopicName().toString();
				String standard = createTopic.getStandard().toString();
				String version = createTopic.getVersion().toString();
				
				Topic topic = new Topic();
				topic.setName(topicNameToCreate);
				topic.setType("standard.topic");
				topic.setMsgType(standard);
				topic.setMsgTypeVersion(version);
				
				List<String> publishAllowedIds = new ArrayList<String>();
				Object publishObj = createTopic.getPublishedAllowed();
				if (publishObj instanceof String) {
					publishAllowedIds.add((String)publishObj);
				} else if(publishObj instanceof List<?>) {
					for (String id : (List<String>)publishObj) {
						publishAllowedIds.add(id);
					}
				}
				topic.setPublishSolutionIDs(publishAllowedIds);
				
				List<String> subscribeAllowedIds = new ArrayList<String>();
				Object subscribeObj = createTopic.getSubscribeAllowed();
				if (subscribeObj instanceof String) {
					subscribeAllowedIds.add((String)subscribeObj);
				} else if(subscribeObj instanceof List<?>) {
					for (String id : (List<String>)subscribeObj) {
						subscribeAllowedIds.add(id);
					}
				}
				topic.setSubscribedSolutionIDs(subscribeAllowedIds);
				
				topicRepo.saveAndFlush(topic);
				if (logController != null) {
					logController.addLog(LogLevels.LOG_LEVEL_INFO,
						"The Topic: " + topic.getName() + " has been created!", true);
				}
			} catch (Exception e) {
				log.error("Error updating the solution!" , e);
			}
		} else if (receivedMessage.getSchema().getName().equalsIgnoreCase("TopicRemoveRequest")) {
			eu.driver.model.core.TopicRemoveRequest removeTopicRequest = (eu.driver.model.core.TopicRemoveRequest) SpecificData.get().deepCopy(eu.driver.model.core.TopicRemoveRequest.SCHEMA$, receivedMessage);
			
			String topicToRemove = removeTopicRequest.getTopicName().toString();
			Topic topic = topicRepo.findObjectByName(topicToRemove);
			List<String> publishIDs = topic.getPublishSolutionIDs();
			List<String> subsriceIDs = topic.getSubscribedSolutionIDs();
			
			for (String id : subsriceIDs) {
				if (!publishIDs.contains(id)) {
					publishIDs.add(id);
				}
			}
			
			TopicRemove topicRemoveMsg = new TopicRemove();
			topicRemoveMsg.setTopicName(topicToRemove);
			for (String id : publishIDs) {
				topicRemoveMsg.setId(id);
				try {
					AdminAdapter.getInstance().sendMessage(topicRemoveMsg, TopicConstants.TOPIC_REMOVE_TOPIC);
				} catch (CommunicationException e) {
					log.error("Error sending TopicRemove message!", e);
				}
			}
			
			topicRepo.delete(topic);
			logController.addLog(LogLevels.LOG_LEVEL_INFO, "The Topic: " + topicToRemove + " has been removed!", true);
			
		}
		log.debug("CreateTopicRequest message received-->");
	}
	
	@ApiOperation(value = "addTopic", nickname = "addTopic")
	@RequestMapping(value = "/AdminService/addTopic", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "topic", value = "the topic that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Topic> addTopic(@RequestBody Topic topic) {
		log.debug("--> addTopic");
		Topic savedTopic = null;
		
		try {
			savedTopic = topicRepo.saveAndFlush(topic);
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Topic: " + topic.getName() + " has been created!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Topic>(savedTopic, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		if (invitesSend) {
			List<TopicInvite> inviteMsgs = topicUtil.createTopicInviteMessages(topic.getName(),
					solutionRepo.findAll(), topic.getPublishSolutionIDs(), topic.getSubscribedSolutionIDs());
			
			mgmtController.sendTopicInvites(topic.getName(), inviteMsgs);
		}
		
		log.debug("--> addTopic");
		return new ResponseEntity<Topic>(savedTopic, HttpStatus.OK);
	}
	
	@ApiOperation(value = "updateTopic", nickname = "updateTopic")
	@RequestMapping(value = "/AdminService/updateTopic", method = RequestMethod.PUT)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "topic", value = "the topic that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Topic> updateTopic(@RequestBody Topic topic) {
		log.debug("--> updateTopic");
		Topic savedTopic = null;
		
		savedTopic = topicRepo.findObjectById(topic.getId());
		
		List<String> oldPublishIds = null;
		List<String> oldSubscribeIds = null;
		
		List<String> newPublishIds = topic.getPublishSolutionIDs();
		List<String> newSubscribeIds = topic.getSubscribedSolutionIDs();
		
		try {
			if (savedTopic != null) {
				oldPublishIds = savedTopic.getPublishSolutionIDs();
				oldSubscribeIds = savedTopic.getSubscribedSolutionIDs();
				savedTopic.setType(topic.getType());
				savedTopic.setName(topic.getName());
				savedTopic.setState(topic.getState());
				if (topic.getMsgType() != null && topic.getMsgTypeNamespace() != null) {
					savedTopic.setMsgTypeNamespace(topic.getMsgTypeNamespace());
					savedTopic.setMsgType(topic.getMsgType());
					if (topic.getMsgTypeVersion() != null) {
						savedTopic.setMsgTypeVersion(topic.getMsgTypeVersion());	
					}
				}
				savedTopic.setDescription(topic.getDescription());
				
				savedTopic.setPublishSolutionIDs(topic.getPublishSolutionIDs());
				savedTopic.setSubscribedSolutionIDs(topic.getSubscribedSolutionIDs());
				
				if (topic.getApplConfigurations() != null) {
					if (savedTopic.getApplConfigurations().size() != topic.getApplConfigurations().size()) {
						savedTopic.setApplConfigurations(topic.getApplConfigurations());
					}
				}
			
				savedTopic = topicRepo.saveAndFlush(savedTopic);
			} else {
				savedTopic = topicRepo.saveAndFlush(topic);
			}
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Topic: " + topic.getName() + " has been updated!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Topic>(savedTopic, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		if (invitesSend) {
			List<String> newInvitePublishIDs = new ArrayList<String>();
			List<String> removeInvitePublishIDs = new ArrayList<String>();
			for (String id : newPublishIds) {
				if (!oldPublishIds.contains(id)) {
					newInvitePublishIDs.add(id);
				}
			}
			for (String id : oldPublishIds) {
				if (!newPublishIds.contains(id)) {
					removeInvitePublishIDs.add(id);
				}
			}
			
			List<String> newInviteSubscribeIDs = new ArrayList<String>();
			List<String> removeInviteSubscribeIDs = new ArrayList<String>();
			for (String id : newSubscribeIds) {
				if (!oldSubscribeIds.contains(id)) {
					newInviteSubscribeIDs.add(id);
				}
			}
			for (String id : oldPublishIds) {
				if (!newSubscribeIds.contains(id)) {
					removeInviteSubscribeIDs.add(id);
				}
			}
			
			for (String id : removeInviteSubscribeIDs) {
				if (!removeInvitePublishIDs.contains(id)) {
					removeInvitePublishIDs.add(id);
				}
			}
			
			List<TopicInvite> inviteMsgs = topicUtil.createTopicInviteMessages(topic.getName(),
					solutionRepo.findAll(), newInvitePublishIDs, newInviteSubscribeIDs);
			
			mgmtController.sendTopicInvites(topic.getName(), inviteMsgs);
			
			TopicRemove topicRemove = new TopicRemove();
			topicRemove.setTopicName(topic.getName());
			
			for (String id : removeInvitePublishIDs) {
				topicRemove.setId(id);
				try {
					AdminAdapter.getInstance().sendMessage(topicRemove, TopicConstants.TOPIC_REMOVE_TOPIC);
				} catch (CommunicationException e) {
					log.error("Error sending TopicRemove message to Client: " + id, e);
				}
			}
		}
		
		log.debug("--> updateTopic");
		return new ResponseEntity<Topic>(savedTopic, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeTopic", nickname = "removeTopic")
	@RequestMapping(value = "/AdminService/removeTopic/{id}", method = RequestMethod.DELETE)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the topic id that should be removed", required = true, dataType = "Long", paramType = "path")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<String> removeTopic(@PathVariable Long id) {
		log.debug("--> removeTopic");
		Topic topic = topicRepo.findObjectById(id);
		List<String> publishIds = topic.getPublishSolutionIDs();
		List<String> subscribeIds = topic.getSubscribedSolutionIDs();
		
		for (String solId : subscribeIds) {
			if (!publishIds.contains(solId)) {
				publishIds.add(solId);
			}
		}
		
		if (topic != null) {
			try {
				topicRepo.delete(id);
			} catch (Exception e) {
				return new ResponseEntity<String>("Error deleting the Topic from the DB!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			TopicRemove topicRemove = new TopicRemove();
			topicRemove.setTopicName(topic.getName());
			for (String solId : publishIds) {
				topicRemove.setId(solId);
				try {
					AdminAdapter.getInstance().sendMessage(topicRemove, TopicConstants.TOPIC_REMOVE_TOPIC);
				} catch (CommunicationException e) {
					log.error("Error sending TopicRemove message to Client: " + id, e);
				}
			}
		}
		
		log.debug("--> removeTopic");
		return new ResponseEntity<String>("Topic removed!", HttpStatus.OK);
	}
	
	public Boolean removeAllTopics() {
		log.debug("--> removeAllTopics");
		try {
			topicRepo.deleteAll();
		} catch (Exception e) {
			log.error("Error removing all Topics!", e);
			return false;
		}
		log.debug("removeAllTopics -->");
		return true;
	}
	
	@ApiOperation(value = "getAllTopics", nickname = "getAllTopics")
	@RequestMapping(value = "/AdminService/getAllTopics", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = TopicList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = TopicList.class),
			@ApiResponse(code = 500, message = "Failure", response = TopicList.class) })
	public ResponseEntity<TopicList> getAllTopics() {
		log.info("--> getAllTopics");
		TopicList topicList = new TopicList();
		
		List<Topic> topics = this.topicRepo.findAll();
		try {
			Collections.sort(topics, (a, b) -> a.getId() < b.getId() ? -1 : 0);
			topicList.setTopics(topics);
		} catch (Exception e) {
			return new ResponseEntity<TopicList>(topicList, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("getAllTopics -->");
		return new ResponseEntity<TopicList>(topicList, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getAllTrialTopics", nickname = "getAllTrialTopics")
	@RequestMapping(value = "/AdminService/getAllTrialTopics", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = TopicList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = TopicList.class),
			@ApiResponse(code = 500, message = "Failure", response = TopicList.class) })
	public ResponseEntity<TopicList> getAllTrialTopics() {
		log.info("--> getAllTrialTopics");
		TopicList topicList = new TopicList();
		
		String configName = null;
		List<Topic> topics = this.topicRepo.findAll();
		TestbedConfig tbConfig = testbedConfigRepo.findActiveConfig(true);
		
		try {
			if (tbConfig != null) {
				configName = tbConfig.getConfigName();
				if (configName != null) {
					Configuration config = configRepo.findObjectByName(configName);
					topics = config.getTopics();
					Collections.sort(topics, (a, b) -> a.getId() < b.getId() ? -1 : 0);
				}
			}
			topicList.setTopics(topics);
		} catch (Exception e) {
			return new ResponseEntity<TopicList>(topicList, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("getAllTrialTopics -->");
		return new ResponseEntity<TopicList>(topicList, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getAllTopicTypes", nickname = "getAllTopicTypes")
	@RequestMapping(value = "/AdminService/getAllTopicTypes", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = List.class),
			@ApiResponse(code = 400, message = "Bad Request", response = List.class),
			@ApiResponse(code = 500, message = "Failure", response = List.class) })
	public List<String> getAllTopicTypes() {
		log.debug("getAllTopicTypes");
		List<String> typeList = new ArrayList<String>();
		typeList.add("core.topic");
		typeList.add("standard.topic");
		
		return typeList;
	}
	
	@ApiOperation(value = "getAllStandards", nickname = "getAllStandards")
	@RequestMapping(value = "/AdminService/getAllStandards", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = List.class),
			@ApiResponse(code = 400, message = "Bad Request", response = List.class),
			@ApiResponse(code = 500, message = "Failure", response = List.class) })
	public List<Standard> getAllStandards() {
		log.debug("getAllStandards");
		
		return this.standardRepo.findAll();
	}
	
	public List<Topic> getAllCoreTopic() {
		log.info("--> getAllCoreTopic");
		
		log.info("getAllCoreTopic -->");
		return this.topicRepo.findObjectByType("core.topic");
	}
	
	public List<Topic> getAllTrialTopicList() {
		log.info("--> getAllTrialTopicList");

		String configName = null;
		List<Topic> topics = this.topicRepo.findAll();
		TestbedConfig tbConfig = testbedConfigRepo.findActiveConfig(true);
		
		try {
			if (tbConfig != null) {
				configName = tbConfig.getConfigName();
				if (configName != null) {
					Configuration config = configRepo.findObjectByName(configName);
					topics = config.getTopics();
				}
			}
		} catch (Exception e) {
			
		}
			
		log.info("getAllTrialTopicList -->");
		return topics;
	}
	
	public void updateTopicState(String topicName, Boolean state) {
		Topic topic = this.topicRepo.findObjectByName(topicName);
		topic.setState(state);
		
		this.topicRepo.saveAndFlush(topic);
		
	}
	
	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
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

	public ConfigurationRepository getConfigRepo() {
		return configRepo;
	}

	public void setConfigRepo(ConfigurationRepository configRepo) {
		this.configRepo = configRepo;
	}

	public TestbedConfigRepository getTestbedConfigRepo() {
		return testbedConfigRepo;
	}

	public void setTestbedConfigRepo(TestbedConfigRepository testbedConfigRepo) {
		this.testbedConfigRepo = testbedConfigRepo;
	}
	
	public Boolean getInvitesSend() {
		return invitesSend;
	}

	public void setInvitesSend(Boolean invitesSend) {
		this.invitesSend = invitesSend;
	}
}
