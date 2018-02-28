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

import eu.driver.admin.service.dto.GatewayList;
import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.helper.FileReader;

@RestController
public class GatewayRESTController {

	private String configJson = "config/gateways.json";
	private Logger log = Logger.getLogger(this.getClass());
	private FileReader fileReader = new FileReader();

	public GatewayRESTController() {

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
		List<Gateway> gateways = new ArrayList<Gateway>();

		String gatewayJson = fileReader.readFile(this.configJson);
		if (gatewayJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(gatewayJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Gateway gateway = new Gateway();
					jsonobject = jsonarray.getJSONObject(i);

					gateway.setId(jsonobject.getString("id"));
					gateway.setName(jsonobject.getString("name"));
					gateway.setState(jsonobject.getBoolean("state"));
					gateway.setDescription(jsonobject.getString("description"));

					ArrayList<String> mangTypes = new ArrayList<String>();     
					JSONArray jArray = jsonobject.getJSONArray("managingTypes");
					if (jArray != null) { 
					   for (int a=0;a<jArray.length();a++){ 
						   mangTypes.add(jArray.getString(a));
					   } 
					} 
					gateway.setManagingType(mangTypes);
					
					gateways.add(gateway);
				}
			} catch (JSONException e) {
				log.error("Error parsind the JSON Gateway response", e);
				return new ResponseEntity<GatewayList>(gatewayList, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		gatewayList.setGateways(gateways);
		log.info("getAllTrialGateways -->");
		return new ResponseEntity<GatewayList>(gatewayList, HttpStatus.OK);
	}

}
