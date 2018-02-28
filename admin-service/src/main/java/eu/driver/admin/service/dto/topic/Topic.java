package eu.driver.admin.service.dto.topic;

import java.util.ArrayList;
import java.util.List;

public class Topic {
	private String id = null;
	private String type = null;
	private String name = null;
	private Boolean state = false;
	private String description = null;
	private List<String> publishSolutionIDs = new ArrayList<String>();
	private List<String> subscribedSolutionIDs = new ArrayList<String>();
	
	public Topic() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public List<String> getPublishSolutionIDs() {
		return publishSolutionIDs;
	}

	public void setPublishSolutionIDs(List<String> publishSolutionIDs) {
		this.publishSolutionIDs = publishSolutionIDs;
	}

	public List<String> getSubscribedSolutionIDs() {
		return subscribedSolutionIDs;
	}

	public void setSubscribedSolutionIDs(List<String> subscribedSolutionIDs) {
		this.subscribedSolutionIDs = subscribedSolutionIDs;
	}
}
