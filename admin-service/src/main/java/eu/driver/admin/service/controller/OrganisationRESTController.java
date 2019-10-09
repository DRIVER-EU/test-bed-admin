package eu.driver.admin.service.controller;

import java.util.List;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
import eu.driver.admin.service.dto.organisation.Organisation;
import eu.driver.admin.service.repository.OrganisationRepository;

@RestController
public class OrganisationRESTController {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	OrganisationRepository orgRepo;
	
	public OrganisationRESTController() {

	}
	
	@ApiOperation(value = "addOrganisation", nickname = "addOrganisation")
	@RequestMapping(value = "/AdminService/addOrganisation", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "organisation", value = "the organisation that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Organisation.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Organisation.class),
			@ApiResponse(code = 500, message = "Failure", response = Organisation.class) })
	public ResponseEntity<Organisation> addOrganisation(@RequestBody Organisation organisation) {
		log.debug("--> addOrganisation");
		Organisation savedOrganisation = null;
		
		try {
			savedOrganisation = orgRepo.saveAndFlush(organisation);
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Organisation: " + organisation.getOrgName() + " has been created!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Organisation>(savedOrganisation, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> addOrganisation");
		return new ResponseEntity<Organisation>(savedOrganisation, HttpStatus.OK);
	}
	
	@ApiOperation(value = "updateOrganisation", nickname = "updateOrganisation")
	@RequestMapping(value = "/AdminService/updateOrganisation", method = RequestMethod.PUT)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "organisation", value = "the organisation that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Organisation.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Organisation.class),
			@ApiResponse(code = 500, message = "Failure", response = Organisation.class) })
	public ResponseEntity<Organisation> updateOrganisation(@RequestBody Organisation organisation) {
		log.debug("--> updateOrganisation");
		Organisation savedOrganisation = null;
		
		savedOrganisation = orgRepo.findObjectById(organisation.getId());
		
		try {
			if (savedOrganisation != null) {
				savedOrganisation.setOrgName(organisation.getOrgName());
				savedOrganisation.setUserName(organisation.getUserName());
				savedOrganisation.setUserPwd(organisation.getUserPwd());
				savedOrganisation.setCertPwd(organisation.getCertPwd());
				savedOrganisation.setEmail(organisation.getEmail());
				savedOrganisation.setCity(organisation.getCity());
				savedOrganisation.setPostcode(organisation.getPostcode());
				savedOrganisation.setStreet(organisation.getStreet());
				savedOrganisation.setNr(organisation.getNr());
				savedOrganisation.setPhone(organisation.getPhone());
				savedOrganisation.setDescription(organisation.getDescription());
				savedOrganisation = orgRepo.saveAndFlush(savedOrganisation);
			} else {
				savedOrganisation = orgRepo.saveAndFlush(organisation);
			}
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Organisation: " + savedOrganisation.getOrgName() + " has been updated!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Organisation>(savedOrganisation, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> updateOrganisation");
		return new ResponseEntity<Organisation>(savedOrganisation, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeOrganisation", nickname = "removeOrganisation")
	@RequestMapping(value = "/AdminService/removeOrganisation/{id}", method = RequestMethod.DELETE)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the organisation id that should be removed", required = true, dataType = "Long", paramType = "path")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> removeOrganisation(@PathVariable Long id) {
		log.debug("--> removeOrganisation");
		
		try {
			orgRepo.delete(id);
		} catch (Exception e) {
			log.error("Error removing the organisation from the DB!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> removeOrganisation");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getAllOrganisations", nickname = "getAllOrganisations")
	@RequestMapping(value = "/AdminService/getAllOrganisations", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = List.class),
			@ApiResponse(code = 400, message = "Bad Request", response = List.class),
			@ApiResponse(code = 500, message = "Failure", response = List.class) })
	public ResponseEntity<List<Organisation>> getAllOrganisations() {
		log.debug("--> getAllOrganisations");
		List<Organisation> organisations = null;

		try {
			organisations = orgRepo.findAll();
		} catch (Exception e) {
			log.error("Error removing the organisation from the DB!", e);
			return new ResponseEntity<List<Organisation>>(organisations, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> getAllOrganisations");
		return new ResponseEntity<List<Organisation>>(organisations, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeAllOrganisations", nickname = "removeAllOrganisations")
	@RequestMapping(value = "/AdminService/removeAllOrganisations", method = RequestMethod.DELETE)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public Boolean removeAllOrganisations() {
		log.debug("--> removeAllOrganisations");
		try {
			orgRepo.deleteAll();
		} catch (Exception e) {
			log.error("Error removing all Organisations!", e);
			return false;
		}
		log.debug("removeAllOrganisations -->");
		return true;
	}

	public OrganisationRepository getOrgRepo() {
		return orgRepo;
	}

	public void setOrgRepo(OrganisationRepository orgRepo) {
		this.orgRepo = orgRepo;
	}

	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}
}
