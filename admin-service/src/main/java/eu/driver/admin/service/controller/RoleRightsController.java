package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.controller.role.ActionMatrix;
import eu.driver.admin.service.controller.role.RoleHandler;
import eu.driver.admin.service.controller.role.Roles;

@RestController
public class RoleRightsController {
	private Logger log = Logger.getLogger(this.getClass());
	
	private ActionMatrix rightsMatrix = new ActionMatrix();
	
	public RoleRightsController() {
		
	}
	
	@ApiOperation(value = "setActiveRole", nickname = "setActiveRole")
	@RequestMapping(value = "/AdminService/setActiveRole", method = RequestMethod.POST )
	@ApiImplicitParams({
		@ApiImplicitParam(name = "activeRole", value = "the role that is active", required = true, dataType = "string", paramType = "query", allowableValues="ADMINISTRATOR,MONITORING") })
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<Boolean> setActiveRile(@RequestParam(value="activeRole", required=true) String activeRole) {
		log.info("--> setActiveRile");
		
		RoleHandler.getInstance().setActiveRole(Roles.valueOf(activeRole));
		
		if (Roles.valueOf(activeRole).equals(Roles.ADMINISTRATOR)) {
			rightsMatrix.setAllTrue();
		} else {
			rightsMatrix.resetState();
		}
		
		log.info("setActiveRile -->");
	    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getActiveRole", nickname = "getActiveRole")
	@RequestMapping(value = "/AdminService/getActiveRole", method = RequestMethod.GET )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<String> getActiveRole() {
		log.info("--> getActiveRole");
		
		log.info("getActiveRole -->");
	    return new ResponseEntity<String>(RoleHandler.getInstance().getActiveRole().toString(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "getRightsMatrix", nickname = "getRightsMatrix")
	@RequestMapping(value = "/AdminService/getRightsMatrix", method = RequestMethod.GET )
	@ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
            @ApiResponse(code = 500, message = "Failure", response = Boolean.class)})
	public ResponseEntity<ActionMatrix> getRightsMatrix() {
		log.info("--> getRightsMatrix");
		
		log.info("getRightsMatrix -->");
	    return new ResponseEntity<ActionMatrix>(rightsMatrix, HttpStatus.OK);
	}

}
