package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Date;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHandler;

import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.WebSocketServer;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSGatewayStateChange;
import eu.driver.admin.service.ws.object.WSLogNotification;
import eu.driver.admin.service.ws.object.WSSolutionStateChange;
import eu.driver.admin.service.ws.object.WSTopicCreationNotification;


@RestController
public class WSNotificationController {
	
	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	@Bean
    public WebSocketHandler wsHandler() {
        return new WebSocketServer();
    }
	
	public WSNotificationController() {

	}
	
	@ApiOperation(value = "sendTopicCreationNotification", nickname = "sendTopicCreationNotification")
	@RequestMapping(value = "/AdminService/sendTopicCreationNotification/{id}", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the id of the topic that is created", required = true, dataType = "string", paramType = "path"),
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendTopicCreationNotification( @PathVariable String id) {
		log.info("--> sendTopicCreationNotification: " + id);
		Boolean send = true;
		
		WSTopicCreationNotification notification = new WSTopicCreationNotification(id, true);
		WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
		
		log.info("sendTopicCreationNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendSolutionStateNotification", nickname = "sendSolutionStateNotification")
	@RequestMapping(value = "/AdminService/sendSolutionStateNotification/{id}", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the id of the solution", required = true, dataType = "string", paramType = "path"),
        @ApiImplicitParam(name = "state", value = "state of the solution", required = true, dataType = "boolean", paramType = "query"),
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendSolutionStateNotification( 	@PathVariable String id,
																	@QueryParam("state") Boolean state ) {
		log.info("--> sendSolutionStateNotification: " + id);
		Boolean send = true;
		
		WSSolutionStateChange notification = new WSSolutionStateChange(id, state);
		WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
		
		log.info("sendSolutionStateNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendGatewayStateNotification", nickname = "sendGatewayStateNotification")
	@RequestMapping(value = "/AdminService/sendGatewayStateNotification/{id}", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the id of the gateway", required = true, dataType = "string", paramType = "path"),
        @ApiImplicitParam(name = "state", value = "state of the gateway", required = true, dataType = "boolean", paramType = "query"),
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendGatewayStateNotification( 	@PathVariable String id,
																	@QueryParam("state") Boolean state ) {
		log.info("--> sendGatewayStateNotification: " + id);
		Boolean send = true;
		
		WSGatewayStateChange notification = new WSGatewayStateChange(id, state);
		WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
		
		log.info("sendGatewayStateNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendLogNotification", nickname = "sendLogNotification")
	@RequestMapping(value = "/AdminService/sendLogNotification/{id}", method = RequestMethod.POST )
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the id of the log record", required = true, dataType = "long", paramType = "path"),
        @ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "clientId", value = "clientId of the log record", required = true, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body")
      })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	@Produces({"application/json"})
	public ResponseEntity<Boolean> sendLogNotification( 	@PathVariable Long id,
															@QueryParam("level") String level,
															@QueryParam("clientId") String clientId,
															@RequestBody String message) {
		log.info("--> sendLogNotification: " + id);
		Boolean send = true;
		
		WSLogNotification notification = new WSLogNotification(id, level, clientId, new Date(), message);
		WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
		
		log.info("sendLogNotification -->");
	    return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}

}
