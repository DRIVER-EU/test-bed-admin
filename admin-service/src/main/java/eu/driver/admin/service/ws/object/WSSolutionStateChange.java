package eu.driver.admin.service.ws.object;

public class WSSolutionStateChange {
	private String action = "SOLUTION";
	private String id = null;
	private Boolean state = false;
	
	public WSSolutionStateChange() {
		
	}
	
	public WSSolutionStateChange(String id, Boolean state) {
		this.id = id;
		this.state = state;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}
}
