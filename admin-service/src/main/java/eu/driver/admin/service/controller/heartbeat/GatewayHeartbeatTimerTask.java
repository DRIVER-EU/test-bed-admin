package eu.driver.admin.service.controller.heartbeat;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import eu.driver.admin.service.controller.GatewayRESTController;
import eu.driver.admin.service.controller.SolutionRESTController;
import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.dto.solution.Solution;

public class GatewayHeartbeatTimerTask extends TimerTask {

	private GatewayRESTController gatewayController =  null;
	private Logger log = Logger.getLogger(this.getClass());
	
	public GatewayHeartbeatTimerTask(GatewayRESTController gatewayController) {
		log.info("GatewayHeartbeatTimerTask");
		this.gatewayController = gatewayController;
	}
	
	public void run() {
		log.debug("GatewayHeartbeatTimerTask -- run");

		List<Gateway> gatewayList = this.gatewayController.getGatewayList();
		long currentTime = (new Date()).getTime();
		
		for (Gateway gateway : gatewayList) {
			currentTime = (new Date()).getTime();
			log.debug("Check HB from gateway: " + gateway.getName());
			if (gateway.getLastHeartBeatReceived() != null) {
				long lastHBTime = gateway.getLastHeartBeatReceived().getTime();
				if (currentTime > (lastHBTime + 12000)) {
					log.debug("No HB revceived from gateway: " + gateway.getName());
					this.gatewayController.updateGatewayState(gateway.getClientId(), gateway.getLastHeartBeatReceived(), null, false);
				}	
			}
		}
		log.debug("GatewayHeartbeatTimerTask -- run end");
	}
}
