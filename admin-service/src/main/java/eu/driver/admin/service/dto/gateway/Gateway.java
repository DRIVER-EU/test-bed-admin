package eu.driver.admin.service.dto.gateway;

import java.util.ArrayList;
import java.util.List;

public class Gateway {
	
	private String id = null;
	private String name = null;
	private Boolean state = false;
	private String description = null;
	private List<String> managingType = new ArrayList<String>();
	
	public Gateway() {
		
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

	public List<String> getManagingType() {
		return managingType;
	}

	public void setManagingType(List<String> managingType) {
		this.managingType = managingType;
	}
}
