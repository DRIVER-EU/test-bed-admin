package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.dto.TopicList;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.helper.FileReader;

@RestController
public class TopicRESTController {
	private String configJson = "config/topics.json";
	private Logger log = Logger.getLogger(this.getClass());
	private FileReader fileReader = new FileReader();

	public TopicRESTController() {

	}
	
	@ApiOperation(value = "getAllTrialTopics", nickname = "getAllTrialTopics")
	@RequestMapping(value = "/AdminService/getAllTrialTopics", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = TopicList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = TopicList.class),
			@ApiResponse(code = 500, message = "Failure", response = TopicList.class) })
	public ResponseEntity<TopicList> getAllTrialTopics() {
		log.info("--> getAllTrialTopics");
		TopicList topicList = new TopicList();
		List<Topic> topics = new ArrayList<Topic>();

		String topicJson = fileReader.readFile(this.configJson);
		if (topicJson != null) {
			try {
				JSONArray jsonarray = new JSONArray(topicJson);
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonobject;
					Topic topic = new Topic();
					jsonobject = jsonarray.getJSONObject(i);

					topic.setId(jsonobject.getString("id"));
					topic.setType(jsonobject.getString("type"));
					topic.setName(jsonobject.getString("name"));
					topic.setState(jsonobject.getBoolean("state"));
					topic.setDescription(jsonobject.getString("description"));

					ArrayList<String> publisher = new ArrayList<String>();     
					JSONArray jArray = jsonobject.getJSONArray("publishSolutionIDs");
					if (jArray != null) { 
					   for (int a=0;a<jArray.length();a++){ 
						   publisher.add(jArray.getString(a));
					   } 
					} 
					topic.setPublishSolutionIDs(publisher);
					
					ArrayList<String> subscriber = new ArrayList<String>();     
					jArray = jsonobject.getJSONArray("subscribedSolutionIDs");
					if (jArray != null) { 
					   for (int a=0;a<jArray.length();a++){ 
						   subscriber.add(jArray.getString(a));
					   } 
					} 
					topic.setSubscribedSolutionIDs(subscriber);
					
					topics.add(topic);
				}
			} catch (JSONException e) {
				log.error("Error parsind the JSON topic response", e);
				return new ResponseEntity<TopicList>(topicList, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		topicList.setTopics(topics);
		log.info("getAllTrialTopics -->");
		return new ResponseEntity<TopicList>(topicList, HttpStatus.OK);
	}
}
