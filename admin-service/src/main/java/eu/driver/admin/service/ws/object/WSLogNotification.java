package eu.driver.admin.service.ws.object;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class WSLogNotification {
	private String mutation = "LOG_NOTIFICATION";
	private Long id = 0L;
	private String level = null;
	private String clientID = null;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date date = null;
	private String message = null;

	public WSLogNotification(Long id, String level, String clientID, Date date, String message) {
		this.id = id;
		this.level = level;
		this.clientID = clientID;
		this.date = date;
		this.message = message;

	}

	public String getMutation() {
		return mutation;
	}

	public void setMutation(String mutation) {
		this.mutation = mutation;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
