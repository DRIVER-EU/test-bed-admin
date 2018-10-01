package eu.driver.admin.service.controller.heartbeat;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

import eu.driver.admin.service.controller.SolutionRESTController;
import eu.driver.admin.service.dto.solution.Solution;

public class HeartbeatTimerTask extends TimerTask {

	private SolutionRESTController solutionController = null;
	private Logger log = Logger.getLogger(this.getClass());
	
	public HeartbeatTimerTask(SolutionRESTController solutionController) {
		log.info("HeartbeatTimerTask");
		this.solutionController = solutionController;
	}
	
	public void run() {
		log.debug("HeartbeatTimerTask -- run");
		List<Solution> solutionList = this.solutionController.getSolutionList();
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long currentTime = zone.convertLocalToUTC(local.getTime(), false);
		//long currentTime = (new Date()).getTime();
		
		for (Solution solution : solutionList) {
			log.debug("Check HB from solution: " + solution.getName());
			if (solution.getLastHeartBeatReceived() != null) {
				long lastHBTime = solution.getLastHeartBeatReceived().getTime();
				if (currentTime > (lastHBTime + 6000)) {
					log.debug("No HB revceived from solution: " + solution.getName());
					this.solutionController.updateSolutionState(solution.getClientId(), solution.getLastHeartBeatReceived(), false);
				}	
			} else {
				this.solutionController.updateSolutionState(solution.getClientId(), solution.getLastHeartBeatReceived(), true);
			}
		}
		log.debug("HeartbeatTimerTask -- run end");
	}
}
