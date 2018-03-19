package eu.driver.admin.service.dto.solution;

import java.util.Date;

public class Solution {
	private String id = null;
	private String name = null;
	private Boolean isAdmin = false;
	private Boolean state = false;
	private String description = null;
	private Date lastHeartBeatReceived = null;
	
	public Solution() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getLastHeartBeatReceived() {
		return lastHeartBeatReceived;
	}

	public void setLastHeartBeatReceived(Date lastHeartBeatReceived) {
		this.lastHeartBeatReceived = lastHeartBeatReceived;
	}
}
