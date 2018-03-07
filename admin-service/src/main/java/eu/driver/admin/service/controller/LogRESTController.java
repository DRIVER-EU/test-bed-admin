package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.dto.LogList;
import eu.driver.admin.service.dto.TopicList;
import eu.driver.admin.service.dto.log.Log;

@RestController
public class LogRESTController {

	private Logger log = Logger.getLogger(this.getClass());

	public LogRESTController() {
		log.info("LogRESTController");
	}

	@ApiOperation(value = "getPagedLogs", nickname = "getPagedLogs")
	@RequestMapping(value = "/AdminService/getPagedLogs", method = RequestMethod.GET)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "the page of the log entries", required = true, dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "size", value = "the size of the log entries", required = true, dataType = "int", paramType = "query")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = LogList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = LogList.class),
			@ApiResponse(code = 500, message = "Failure", response = LogList.class) })
	public ResponseEntity<LogList> getPagedLogs(@QueryParam("page") int page, @QueryParam("size") int size) {
		log.info("-->getPagedLogs: " + page + ", " + size);
		LogList result = new LogList();
		List<Log> logs = new ArrayList<Log>();

		// todo, get the logs and return them
		
		log.info("getPagedLogs-->");
		result.setLogs(logs);
		return new ResponseEntity<LogList>(result, HttpStatus.OK);
	}

}
