package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.ValidationException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.dto.LogList;
import eu.driver.admin.service.dto.log.Log;
import eu.driver.admin.service.repository.LogRepository;
import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSLogNotification;
import eu.driver.api.IAdaptorCallback;

@RestController
public class LogRESTController implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	private StringJSONMapper mapper = new StringJSONMapper();
	
	private Double currentPageSize = 20D;
	
	private String clientId = ClientProperties.getInstance().getProperty("client.id");
	
	@Autowired
	LogRepository logRepo;
	
	@PersistenceContext(unitName = "AdminService")
	private EntityManager entityManager;

	public LogRESTController() {
		log.info("LogRESTController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage, String topicName) {
		log.debug("log message received!");
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Log")) {
			try {
				eu.driver.model.core.Log logMsg = (eu.driver.model.core.Log) SpecificData.get().deepCopy(eu.driver.model.core.Log.SCHEMA$, receivedMessage); 
				// store the log message
				
				Log dbLog = new Log();
				String clientId = logMsg.getId().toString();
				
				if  (clientId.length() > 255) {
					log.warn("Received client is longer than 255 char: " + clientId);
					clientId = clientId.substring(0, 245);
				}
				dbLog.setClientId(clientId);
				dbLog.setLevel(logMsg.getLevel().toString());
				dbLog.setSendDate(new Date(logMsg.getDateTimeSent()));
				dbLog.setMessage(logMsg.getLog().toString());
				
				if (logRepo != null) {
					dbLog = logRepo.saveAndFlush(dbLog);
				}
	
				sendWSNotification(dbLog);
			} catch (ValidationException vEx) {
				log.error("Error storing the log record into the DB!", vEx);
			} catch (Exception e) {
				log.error("Error processing the Log message received!", e);
			}
		}

	}
	
	@ApiOperation(value = "getAllLogs", nickname = "getAllLogs")
	@RequestMapping(value = "/AdminService/getAllLogs", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", value = "the act. page of the client", required = false, dataType = "int", paramType = "query"),
		@ApiImplicitParam(name = "size", value = "the act. size of the page records on the client", required = false, dataType = "int", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = LogList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = LogList.class),
			@ApiResponse(code = 500, message = "Failure", response = LogList.class) })
	public ResponseEntity<LogList> getAllLogs(
			@RequestParam(value="page", required=false) Integer page,
			@RequestParam(value="size", required=false) Integer size) {
		log.debug("-->getAllLogs");
		LogList result = new LogList();
		
		String query = "SELECT NEW Log(i.id, i.clientId, i.level, i.sendDate, i.message) FROM Log i";
		
		query += " ORDER BY i.id DESC";

		TypedQuery<Log> typedQuery = entityManager.createQuery(query, Log.class);
		if (page != null) {
			page--;
			if (size == null) {
				size = 20;
			}
			typedQuery.setFirstResult(page * size);
			typedQuery.setMaxResults(size);
			
			this.currentPageSize = Double.valueOf(size);
		}
		List<Log> logs = typedQuery.getResultList();
		
		log.debug("getAllLogs-->");
		result.setLogs(logs);
		return new ResponseEntity<LogList>(result, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getPageCount", nickname = "getPageCount")
	@RequestMapping(value = "/AdminService/getPageCount", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = ArrayList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ArrayList.class),
			@ApiResponse(code = 500, message = "Failure", response = ArrayList.class) })
	public ResponseEntity<Double> getPageCount() {
		log.info("-->getPageCount");
		String query = "SELECT NEW Log(i.id) FROM Log i";
		TypedQuery<Log> typedQuery = entityManager.createQuery(query, Log.class);
		
		List<Log> records = typedQuery.getResultList();
		
		Double recCount = Double.valueOf(records.size());
		
		Double pageCount = 1D;
		if (recCount > 0 && this.currentPageSize > 0) {
			pageCount = Math.ceil(recCount/this.currentPageSize);
		}

		log.info("getPageCount-->");
		return new ResponseEntity<Double>(pageCount, HttpStatus.OK);
	}

	public Log addLog(String level, String message, Boolean sendNotification) {
		Log log = new Log();
		log.setClientId(clientId);
		log.setLevel(level);
		log.setSendDate(new Date());
		log.setMessage(message);
		
		Log dbLog	= logRepo.saveAndFlush(log);
		
		if (sendNotification) {
			sendWSNotification(dbLog);
		}
		return dbLog;
	}
	
	public Boolean removeAllLogs() {
		log.debug("--> removeAllLogs");
		try {
			logRepo.deleteAll();
		} catch (Exception e) {
			log.error("Error removing all Logs!", e);
			return false;
		}
		log.debug("removeAllLogs -->");
		return true;
	}
	
	private void sendWSNotification(Log dbLog) {
		// send the log message via the ws connection
		WSLogNotification logNotification = new WSLogNotification(dbLog.getId(),
				dbLog.getLevel(), dbLog.getClientId(),dbLog.getSendDate(), dbLog.getMessage());
		WSController.getInstance().sendMessage(
				mapper.objectToJSONString(logNotification));
	}

	public LogRepository getLogRepo() {
		return logRepo;
	}

	public void setLogRepo(LogRepository logRepo) {
		this.logRepo = logRepo;
	}

}
