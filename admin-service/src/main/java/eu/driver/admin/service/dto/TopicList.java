package eu.driver.admin.service.dto;

import java.util.ArrayList;
import java.util.List;

import eu.driver.admin.service.dto.topic.Topic;

public class TopicList {
	private List<Topic> topics = new ArrayList<Topic>();
	
	public TopicList() {
		
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}
}
