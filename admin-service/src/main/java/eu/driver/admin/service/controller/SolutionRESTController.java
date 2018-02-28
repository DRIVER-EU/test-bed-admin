package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.dto.SolutionList;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.helper.FileReader;

@RestController
public class SolutionRESTController {

	private String configJson = "config/solutions.json";
	private Logger log = Logger.getLogger(this.getClass());
	private FileReader fileReader = new FileReader();

	public SolutionRESTController() {

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
		List<Solution> solutions = new ArrayList<Solution>();

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

		solutionList.setSolutions(solutions);
		log.info("getAllTrialSolutions -->");
		return new ResponseEntity<SolutionList>(solutionList, HttpStatus.OK);
	}
}
