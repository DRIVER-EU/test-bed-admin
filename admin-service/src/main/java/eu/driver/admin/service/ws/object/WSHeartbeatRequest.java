package eu.driver.admin.service.ws.object;

import java.util.Date;

public class WSHeartbeatRequest {
	private String requestId;
	private String mutation = "HBREQUEST";
	private Date sendTime;
	
	public WSHeartbeatRequest() {
		
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMutation() {
		return mutation;
	}

	public void setAction(String mutation) {
		this.mutation = mutation;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	
	public WSHeartbeatResponse createResponse() {
		WSHeartbeatResponse response = new WSHeartbeatResponse();
		
		response.setRequestId(this.requestId);
		response.setSendTime(new Date());
		response.setState("OK");
		
		return response;
	}
}
