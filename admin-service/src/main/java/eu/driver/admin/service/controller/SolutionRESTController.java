package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
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
import eu.driver.admin.service.controller.heartbeat.HeartbeatTimerTask;
import eu.driver.admin.service.dto.SolutionList;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.helper.FileReader;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSSolutionStateChange;
import eu.driver.api.IAdaptorCallback;

@RestController
public class SolutionRESTController implements IAdaptorCallback {
	private Logger log = Logger.getLogger(this.getClass());
	
	private Timer hbTimer = null;
	private HeartbeatTimerTask hbTimerTask = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	SolutionRepository solutionRepo;

	public SolutionRESTController() {
		log.info("-->SolutionRESTController");
		
		hbTimerTask = new HeartbeatTimerTask(this);
		hbTimer = new Timer();
		hbTimer.schedule(hbTimerTask, 3000, 3000);
		log.info("-->SolutionRESTController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage) {
		log.debug("SolutionRESTController-->");
		
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Heartbeat")) {
			try {
				eu.driver.model.core.Heartbeat hbMsg = (eu.driver.model.core.Heartbeat) SpecificData.get().deepCopy(eu.driver.model.core.Heartbeat.SCHEMA$, receivedMessage);
				String clientID = hbMsg.getId().toString();
				log.debug("HB From client received: " + clientID);
				updateSolutionState(clientID, new Date(hbMsg.getAlive()), true);
			} catch (Exception e) {
				log.error("Error updating the solution!" , e);
			}
		}
		log.debug("heartbeat message received-->");
	}
	
	public void updateSolutionState(String clientID, Date isAliveDate, Boolean state) {
		log.debug("-->updateSolutionState");
		try {
			Solution solution = this.solutionRepo.findObjectByClientId(clientID);
			solution.setLastHeartBeatReceived(isAliveDate);
			
			if (solution.getState() != state) {
				solution.setState(state);
				if (logController != null) {
					logController.addLog(LogLevels.LOG_LEVEL_INFO,
						"The Solution: " + solution.getName() + " changed its state to: " + solution.getState(),
						true);
				}
				WSSolutionStateChange notification = new WSSolutionStateChange(solution.getClientId(), solution.getState());
				WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
			}
			this.solutionRepo.saveAndFlush(solution);
		} catch (Exception e) {
			log.error("Error updating the solution!" , e);
		}
		log.debug("updateSolutionState-->");	
	}
	
	@ApiOperation(value = "addSolution", nickname = "addSolution")
	@RequestMapping(value = "/AdminService/addSolution", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "solution", value = "the solution that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Solution> addSolution(@RequestBody Solution solution) {
		log.debug("--> addSolution");
		Solution savedSolution = null;
		
		try {
			savedSolution = solutionRepo.saveAndFlush(solution);
		} catch (Exception e) {
			return new ResponseEntity<Solution>(savedSolution, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> addSolution");
		return new ResponseEntity<Solution>(savedSolution, HttpStatus.OK);
	}
	
	@ApiOperation(value = "updateSolution", nickname = "updateSolution")
	@RequestMapping(value = "/AdminService/updateSolution", method = RequestMethod.PUT)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "solution", value = "the solution that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Solution> updateSolution(@RequestBody Solution solution) {
		log.debug("--> updateSolution");
		Solution savedSolution = null;
		
		savedSolution = solutionRepo.findObjectById(solution.getId());
		
		try {
			if (savedSolution != null) {
				savedSolution.setDescription(solution.getDescription());
				savedSolution.setClientId(solution.getClientId());
				savedSolution.setIsAdmin(solution.getIsAdmin());
				savedSolution.setIsService(solution.getIsService());
				savedSolution.setName(solution.getName());
				savedSolution.setState(solution.getState());
				savedSolution.setLastHeartBeatReceived(solution.getLastHeartBeatReceived());
				
				savedSolution = solutionRepo.saveAndFlush(savedSolution);
			} else {
				savedSolution = solutionRepo.saveAndFlush(solution);
			}
		} catch (Exception e) {
			return new ResponseEntity<Solution>(savedSolution, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> updateSolution");
		return new ResponseEntity<Solution>(savedSolution, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeSolution", nickname = "removeSolution")
	@RequestMapping(value = "/AdminService/removeSolution/{id}", method = RequestMethod.DELETE)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the solution id that should be removed", required = true, dataType = "Long", paramType = "path")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<String> removeSolution(@PathVariable Long id) {
		log.debug("--> removeSolution");
		
		try {
			solutionRepo.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<String>("Error deleting the Solution from the DB!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> removeSolution");
		return new ResponseEntity<String>("Solution removed!", HttpStatus.OK);
	}

	@ApiOperation(value = "getAllTrialSolutions", nickname = "getAllTrialSolutions")
	@RequestMapping(value = "/AdminService/getAllTrialSolutions", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = SolutionList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = SolutionList.class),
			@ApiResponse(code = 500, message = "Failure", response = SolutionList.class) })
	public ResponseEntity<SolutionList> getAllTrialSolutions() {
		log.info("--> getAllTrialSolutions");
		SolutionList solutionList = new SolutionList();
		
		List<Solution> solutions = this.solutionRepo.findAll();
		solutionList.setSolutions(solutions);

		log.info("getAllTrialSolutions -->");
		return new ResponseEntity<SolutionList>(solutionList, HttpStatus.OK);
	}
	
	public List<Solution> getSolutionList() {
		log.debug("--> getSolutionList");
		
		log.debug("getSolutionList -->");
		return this.solutionRepo.findAll();
	}
	
	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}

	public SolutionRepository getSolutionRepo() {
		return solutionRepo;
	}

	public void setSolutionRepo(SolutionRepository solutionRepo) {
		this.solutionRepo = solutionRepo;
	}
}
