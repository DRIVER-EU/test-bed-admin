package eu.driver.admin.service.controller.heartbeat;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import eu.driver.admin.service.controller.SolutionRESTController;
import eu.driver.admin.service.dto.solution.Solution;

public class SolutionHeartbeatTimerTask extends TimerTask {

	private SolutionRESTController solutionController = null;
	private Logger log = Logger.getLogger(this.getClass());
	
	public SolutionHeartbeatTimerTask(SolutionRESTController solutionController) {
		log.info("SolutionHeartbeatTimerTask");
		this.solutionController = solutionController;
	}
	
	public void run() {
		log.debug("SolutionHeartbeatTimerTask -- run");
		List<Solution> solutionList = this.solutionController.getSolutionList();
		long currentTime = (new Date()).getTime();
		
		for (Solution solution : solutionList) {
			currentTime = (new Date()).getTime();
			log.debug("Check HB from solution: " + solution.getName());
			if (solution.getLastHeartBeatReceived() != null) {
				long lastHBTime = solution.getLastHeartBeatReceived().getTime();
				if (currentTime > (lastHBTime + 12000)) {
					log.debug("No HB revceived from solution: " + solution.getName());
					this.solutionController.updateSolutionState(solution.getClientId(), solution.getLastHeartBeatReceived(), null, false);
				}	
			}
		}
		log.debug("SolutionHeartbeatTimerTask -- run end");
	}
}
