package eu.driver.admin.service.controller;

import java.util.Date;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.api.IAdaptorCallback;

@RestController
public class HeartbeatController implements IAdaptorCallback {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	SolutionRESTController solutionCtrl;
	
	@Autowired
	GatewayRESTController gatewayCtrl;
	
	public HeartbeatController() {
		log.info("-->HeartbeatController");
	}
	
	@Override
	public void messageReceived(IndexedRecord key, IndexedRecord receivedMessage, String topicName) {
		log.debug("heartbeat message received-->");
		
		if (receivedMessage.getSchema().getName().equalsIgnoreCase("Heartbeat")) {
			try {
				eu.driver.model.core.Heartbeat hbMsg = (eu.driver.model.core.Heartbeat) SpecificData.get().deepCopy(eu.driver.model.core.Heartbeat.SCHEMA$, receivedMessage);
				String clientID = hbMsg.getId().toString();
				String origin = hbMsg.getOrigin().toString();
				log.debug("HB From client received: " + clientID);
				solutionCtrl.updateSolutionState(clientID, new Date(hbMsg.getAlive()), origin, true);
				gatewayCtrl.updateGatewayState(clientID, new Date(hbMsg.getAlive()), origin, true);
			} catch (Exception e) {
				log.error("Error updating the solution!" , e);
			}
		}
		log.debug("heartbeat message received-->");
	}

}
