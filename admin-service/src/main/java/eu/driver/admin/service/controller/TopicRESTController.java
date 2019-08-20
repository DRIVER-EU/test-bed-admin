package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.dto.TopicList;
import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.configuration.TestbedConfig;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.standard.Standard;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.StandardRepository;
import eu.driver.admin.service.repository.TestbedConfigRepository;
import eu.driver.admin.service.repository.TopicRepository;

@RestController
public class TopicRESTController {
	private Logger log = Logger.getLogger(this.getClass());
	
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
	

	public TopicRESTController() {
		log.info("--> TopicRESTController");
		
		log.info("TopicRESTController -->");
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
		
		// ToDo: send the invites
		
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
		
		try {
			if (savedTopic != null) {
				savedTopic.setType(topic.getType());
				savedTopic.setName(topic.getName());
				savedTopic.setState(topic.getState());
				savedTopic.setMsgType(topic.getMsgType());
				savedTopic.setMsgTypeVersion(topic.getMsgTypeVersion());
				savedTopic.setDescription(topic.getDescription());
				savedTopic.setPublishSolutionIDs(topic.getPublishSolutionIDs());
				savedTopic.setSubscribedSolutionIDs(topic.getSubscribedSolutionIDs());
				
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
		
		// ToDo: send the invites
		
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
		
		try {
			topicRepo.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<String>("Error deleting the Topic from the DB!", HttpStatus.INTERNAL_SERVER_ERROR);
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
	
	public List<Topic> getAllTrialTopic() {
		log.info("--> getAllTrialTopics");

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
			
		log.info("getAllTrialTopics -->");
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
}
