package eu.driver.admin.service.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.controller.heartbeat.HeartbeatTimerTask;
import eu.driver.admin.service.dto.SolutionList;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.helper.FileReader;
import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSSolutionStateChange;
import eu.driver.api.IAdaptorCallback;

@RestController
public class SolutionRESTController implements IAdaptorCallback {

	private String configJson = "config/solutions.json";
	private Logger log = Logger.getLogger(this.getClass());
	private FileReader fileReader = new FileReader();
	
	private Timer hbTimer = null;
	private HeartbeatTimerTask hbTimerTask = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	
	private List<Solution> solutions = new ArrayList<Solution>();

	public SolutionRESTController() {
		hbTimerTask = new HeartbeatTimerTask(this);
		hbTimer = new Timer();
		hbTimer.schedule(hbTimerTask, 3000, 3000);
	}
	
	@Override
	public void messageReceived(IndexedRecord receivedMessage) {
		log.info("-->heartbeat message received!");
		
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Heartbeat")) {
			try {
				eu.driver.model.core.Heartbeat hbMsg = (eu.driver.model.core.Heartbeat) SpecificData.get().deepCopy(eu.driver.model.core.Heartbeat.SCHEMA$, receivedMessage);
				String clientID = hbMsg.getId().toString();
				for (Solution solution : solutions) {
					if (solution.getId().equalsIgnoreCase(clientID)) {
						solution.setLastHeartBeatReceived(new Date(hbMsg.getAlive()));
						updateSolutionState(clientID, true);
					}
				}
			} catch (Exception e) {
				log.error("Error updating the solution!" , e);
			}
		}
		log.info("heartbeat message received-->");
	}
	
	public void updateSolutionState(String clientID, Boolean state) {
		log.info("-->updateSolutionState");
		try {
			for (Solution solution : solutions) {
				if (solution.getId().equalsIgnoreCase(clientID)) {
					if (solution.getState() != state) {
						solution.setState(state);
						WSSolutionStateChange notification = new WSSolutionStateChange(solution.getId(), solution.getState());
						WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
					}
				}
			}
		} catch (Exception e) {
			log.error("Error updating the solution!" , e);
		}
		log.info("updateSolutionState-->");	
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
		
		if (solutions.size() > 0) {
			solutionList.setSolutions(solutions);
		} else {
			String solutionJson = fileReader.readFile(this.configJson);
			if (solutionJson != null) {
				try {
					JSONArray jsonarray = new JSONArray(solutionJson);
					for (int i = 0; i < jsonarray.length(); i++) {
						JSONObject jsonobject;
						Solution solution = new Solution();
						jsonobject = jsonarray.getJSONObject(i);

						solution.setId(jsonobject.getString("id"));
						solution.setName(jsonobject.getString("name"));
						solution.setState(jsonobject.getBoolean("state"));
						solution.setDescription(jsonobject.getString("description"));
						
						solutions.add(solution);
					}
				} catch (JSONException e) {
					log.error("Error parsind the JSON solution response", e);
					return new ResponseEntity<SolutionList>(solutionList, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}

		solutionList.setSolutions(solutions);
		log.info("getAllTrialSolutions -->");
		return new ResponseEntity<SolutionList>(solutionList, HttpStatus.OK);
	}
	
	public List<Solution> getSolutionList() {
		log.info("--> getSolutionList");
		
		log.info("getSolutionList -->");
		return this.solutions;
	}
}
