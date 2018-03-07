package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.admin.service.kafka.KafkaAdminController;
import eu.driver.model.core.AdminHeartbeat;
import eu.driver.model.core.Heartbeat;
import eu.driver.model.core.Timing;
import eu.driver.model.edxl.EDXLDistribution;
import eu.driver.model.system.Log;
import eu.driver.model.system.TopicCreate;
import eu.driver.model.system.TopicInvite;

@RestController
public class MgmtController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private KafkaAdminController adminController = new KafkaAdminController();

	
	public MgmtController() {

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
		
		try {
			createAllCoreTopics();
		} catch (Exception e) {
			log.error("Error creating the Core Topics!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
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
		Boolean send = true;
		
		try {
			createTrialTopics();
		} catch (Exception e) {
			log.error("Error creating the Trial Topics!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("startTrialConfig -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	private void createAllCoreTopics() throws Exception {
		adminController.createTopic(TopicConstants.ADMIN_HEARTBEAT_TOPIC, new EDXLDistribution(), new AdminHeartbeat());
		adminController.createTopic(TopicConstants.HEARTBEAT_TOPIC, new EDXLDistribution(), new Heartbeat());
		adminController.createTopic(TopicConstants.LOGGING_TOPIC, new EDXLDistribution(), new Log());
		adminController.createTopic(TopicConstants.TIMING_TOPIC, new EDXLDistribution(), new Timing());
		adminController.createTopic(TopicConstants.TOPIC_INVITE_TOPIC, new EDXLDistribution(), new TopicInvite());
		adminController.createTopic(TopicConstants.TOPIC_CREATE_REQUEST_TOPIC, new EDXLDistribution(), new TopicCreate());
	}
	
	private void createTrialTopics() throws Exception {
		
	
	}

}
