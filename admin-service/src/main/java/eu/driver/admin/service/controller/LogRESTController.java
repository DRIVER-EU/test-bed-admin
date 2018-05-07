package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Date;
import java.util.List;



import javax.validation.ValidationException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	private String clientId = ClientProperties.getInstance().getProperty("client.id");
	
	@Autowired
	LogRepository logRepo;

	public LogRESTController() {
		log.info("LogRESTController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage) {
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
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = LogList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = LogList.class),
			@ApiResponse(code = 500, message = "Failure", response = LogList.class) })
	public ResponseEntity<LogList> getAllLogs() {
		log.debug("-->getAllLogs");
		LogList result = new LogList();
		List<Log> logs = logRepo.findAll();
		
		log.debug("getAllLogs-->");
		result.setLogs(logs);
		return new ResponseEntity<LogList>(result, HttpStatus.OK);
	}

	public void addLog(String level, String message, Boolean sendNotification) {
		Log log = new Log();
		log.setClientId(clientId);
		log.setLevel(level);
		log.setSendDate(new Date());
		log.setMessage(message);
		
		Log dbLog	= logRepo.saveAndFlush(log);
		
		if (sendNotification) {
			sendWSNotification(dbLog);
		}
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
