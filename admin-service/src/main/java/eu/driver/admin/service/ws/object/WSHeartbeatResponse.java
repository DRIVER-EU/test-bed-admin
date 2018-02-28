package eu.driver.admin.service.ws.object;

import java.util.Date;

public class WSHeartbeatResponse {
	private String requestId;
	private String action = "HBRESPONSE";
	private Date sendTime;
	private String state;
	
	public WSHeartbeatResponse() {
		
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
