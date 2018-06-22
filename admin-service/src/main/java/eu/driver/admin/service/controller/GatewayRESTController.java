package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.dto.GatewayList;
import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.helper.FileReader;
import eu.driver.admin.service.repository.GatewayRepository;

@RestController
public class GatewayRESTController {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	GatewayRepository gwRepo;
	
	public GatewayRESTController() {

	}
	
	@ApiOperation(value = "addGateway", nickname = "addGateway")
	@RequestMapping(value = "/AdminService/addGateway", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "solution", value = "the solution that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Gateway> addGateway(@RequestBody Gateway gateway) {
		log.debug("--> addGateway");
		Gateway savedGateway = null;
		
		try {
			savedGateway = gwRepo.saveAndFlush(gateway);
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Gateway: " + gateway.getName() + " has been created!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Gateway>(savedGateway, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> addGateway");
		return new ResponseEntity<Gateway>(savedGateway, HttpStatus.OK);
	}
	
	@ApiOperation(value = "updateGateway", nickname = "updateGateway")
	@RequestMapping(value = "/AdminService/updateGateway", method = RequestMethod.PUT)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "solution", value = "the solution that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Gateway> updateGateway(@RequestBody Gateway gateway) {
		log.debug("--> updateGateway");
		Gateway savedGateway = null;
		
		savedGateway = gwRepo.findObjectById(gateway.getId());
		
		try {
			if (savedGateway != null) {
				savedGateway.setDescription(gateway.getDescription());
				savedGateway.setClientId(gateway.getClientId());
				savedGateway.setName(gateway.getName());
				savedGateway.setState(gateway.getState());
				
				savedGateway = gwRepo.saveAndFlush(savedGateway);
			} else {
				savedGateway = gwRepo.saveAndFlush(gateway);
			}
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Gateway: " + gateway.getName() + " has been updated!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Gateway>(savedGateway, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> updateGateway");
		return new ResponseEntity<Gateway>(savedGateway, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeGateway", nickname = "removeGateway")
	@RequestMapping(value = "/AdminService/removeGateway/{id}", method = RequestMethod.DELETE)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the gateway id that should be removed", required = true, dataType = "Long", paramType = "path")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<String> removeGateway(@PathVariable Long id) {
		log.debug("--> removeGateway");
		
		try {
			gwRepo.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<String>("Error deleting the Gateway from the DB!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> removeGateway");
		return new ResponseEntity<String>("Gateway removed!", HttpStatus.OK);
	}
	
	public Boolean removeAllGateways() {
		log.debug("--> removeAllGateways");
		try {
			gwRepo.deleteAll();
		} catch (Exception e) {
			log.error("Error removing all Gateways!", e);
			return false;
		}
		log.debug("removeAllGateways -->");
		return true;
	}

	@ApiOperation(value = "getAllTrialGateways", nickname = "getAllTrialGateways")
	@RequestMapping(value = "/AdminService/getAllTrialGateways", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = GatewayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GatewayList.class),
			@ApiResponse(code = 500, message = "Failure", response = GatewayList.class) })
	public ResponseEntity<GatewayList> getAllTrialGateways() {
		log.info("--> getAllTrialGateways");
		GatewayList gatewayList = new GatewayList();

		gatewayList.setGateways(this.gwRepo.findAll());
		log.info("getAllTrialGateways -->");
		return new ResponseEntity<GatewayList>(gatewayList, HttpStatus.OK);
	}

	public GatewayRepository getGwRepo() {
		return gwRepo;
	}

	public void setGwRepo(GatewayRepository gwRepo) {
		this.gwRepo = gwRepo;
	}
	
	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}
}
