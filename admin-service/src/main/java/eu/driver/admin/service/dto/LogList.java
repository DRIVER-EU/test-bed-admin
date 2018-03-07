package eu.driver.admin.service.dto;

import java.util.ArrayList;
import java.util.List;

import eu.driver.admin.service.dto.log.Log;

public class LogList {
	private List<Log> logs = new ArrayList<Log>();
	
	public LogList() {
		
	}

	public List<Log> getLogs() {
		return logs;
	}

	public void setLogs(List<Log> logs) {
		this.logs = logs;
	}
	
}
