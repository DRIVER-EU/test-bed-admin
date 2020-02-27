package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.repository.TopicRepository;

@RestController
public class ConfigurationController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	ConfigurationRepository configRepo;
	
	@Autowired
	SolutionRepository solutionRepo;
	
	@Autowired
	TopicRepository topicRepo;
	
	public ConfigurationController() {
		
	}
	
	@ApiOperation(value = "getAllConfigurations", nickname = "getAllConfigurations")
	@RequestMapping(value = "/AdminService/getAllConfigurations", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = List.class),
			@ApiResponse(code = 400, message = "Bad Request", response = List.class),
			@ApiResponse(code = 500, message = "Failure", response = List.class) })
	public ResponseEntity<List<Configuration>> getAllConfigurations() {
		log.info("-->getAllConfigurations");
		List<Configuration> configurations =  null;
		
		configurations = configRepo.findAll();
		
		log.info("getAllConfigurations-->");
		return new ResponseEntity<List<Configuration>>(configurations, HttpStatus.OK);
	}
	
	@ApiOperation(value = "addConfiguration", nickname = "addConfiguration")
	@RequestMapping(value = "/AdminService/addConfiguration", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "configuration", value = "the configuration that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Configuration> addConfiguration(@RequestBody Configuration newConfiguration) {
		log.info("-->addConfiguration");
		Configuration savedConfiguration = null;
		try {
			Configuration configuration = configRepo.findObjectByName(newConfiguration.getName());
			if (configuration == null) {
				configuration = new Configuration();

				configuration.setName(newConfiguration.getName());
				configuration.setDiscription(newConfiguration.getDiscription());
				configuration = configRepo.saveAndFlush(configuration);
				
				List<Solution> newSolutions = newConfiguration.getSolutions();
				List<Solution> solutions = new ArrayList<Solution>();
				for (Solution newSolution : newSolutions) {
					try {
					   Solution sol = solutionRepo.findObjectById(newSolution.getId());
					   if (sol != null) {
						   sol.addApplSolConfigurations(configuration);
						   solutions.add(sol);
					   }
				   } catch (Exception  e) {
					   log.error("The solution: " + newSolution.getName() + " defined in the configuration cannot be found!");
				   }
				}
				configuration.setSolutions(solutions);
				
				List<Topic> newTopics = newConfiguration.getTopics(); 
				List<Topic> topics = new ArrayList<Topic>();
				for (Topic newTopic : newTopics) {
					try {
					   Topic top = topicRepo.findObjectById(newTopic.getId());
					   top.addApplConfigurations(configuration);
					   if (top != null) {
						   topics.add(top);
					   }
				   } catch (Exception  e) {
					   log.error("The topic: " + newTopic.getName() + " defined in the configuration cannot be found!");
				   }
				}
				configuration.setTopics(topics);

				configRepo.saveAndFlush(configuration);
			} else {
				log.info("The configuration " + newConfiguration.getName() + " is already available!");
				return new ResponseEntity<Configuration>(newConfiguration, HttpStatus.FOUND);
			}
			
			savedConfiguration = configRepo.saveAndFlush(configuration);
		} catch (Exception e) {
			log.error("Error storing the configuration!", e);
			return new ResponseEntity<Configuration>(savedConfiguration, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("addConfiguration-->");
		return new ResponseEntity<Configuration>(savedConfiguration, HttpStatus.OK);
	}
	
	@ApiOperation(value = "updateConfiguration", nickname = "updateConfiguration")
	@RequestMapping(value = "/AdminService/updateConfiguration", method = RequestMethod.PUT)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "configuration", value = "the configuration that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Configuration> updateConfiguration(@RequestBody Configuration configuration) {
		log.info("-->updateConfiguration");
		Configuration savedConfiguration = null;
		try {
			savedConfiguration = configRepo.findObjectById(configuration.getId());
			if (savedConfiguration != null) {
				savedConfiguration.setName(configuration.getName());
				savedConfiguration.setDiscription(configuration.getDiscription());
				
				List<Solution> newSolutions = configuration.getSolutions();
				List<Solution> currentSolutions = savedConfiguration.getSolutions();
				List<Long> solutionsToAdd = new ArrayList<Long>();
				List<Long> solutionsToRemove = new ArrayList<Long>();
				
				List<Topic> newTopics = configuration.getTopics();
				List<Topic> currentTopics = savedConfiguration.getTopics();
				List<Long> topicsToAdd = new ArrayList<Long>();
				List<Long> topicsToRemove = new ArrayList<Long>();
				
				
				// Solutions
				for (Solution solution : newSolutions) {
					boolean solToAdd = true;
					for (Solution curSolution : currentSolutions) {
						if (curSolution.getId().equals(solution.getId())) {
							solToAdd = false;
						}
					}
					if (solToAdd) {
						solutionsToAdd.add(solution.getId());
					}
				}
				
				for (Solution curSolution : currentSolutions) {
					boolean solToRemove = true;
					for (Solution solution : newSolutions) {
						if (curSolution.getId().equals(solution.getId())) {
							solToRemove = false;
						}
					}
					if (solToRemove) {
						solutionsToRemove.add(curSolution.getId());
					}
				}
				
				for (Long id : solutionsToAdd) {
					Solution solution = solutionRepo.findObjectById(id);
					solution.addApplSolConfigurations(savedConfiguration);
					savedConfiguration.addSolution(solution);
				}
				
				for (Long id : solutionsToRemove) {
					Solution solution = solutionRepo.findObjectById(id);
					solution.getApplSolConfigurations().remove(savedConfiguration);
					savedConfiguration.getSolutions().remove(solution);
				}
				
				// Topics
				for (Topic topic : newTopics) {
					boolean topicToAdd = true;
					for (Topic curTopic : currentTopics) {
						if (curTopic.getId().equals(topic.getId())) {
							topicToAdd = false;
						}
					}
					if (topicToAdd) {
						topicsToAdd.add(topic.getId());
					}
				}
				
				for (Topic curTopic : currentTopics) {
					boolean topicToRemove = true;
					for (Topic topic : newTopics) {
						if (curTopic.getId().equals(topic.getId())) {
							topicToRemove = false;
						}
					}
					if (topicToRemove) {
						topicsToRemove.add(curTopic.getId());
					}
				}
				
				for (Long id : topicsToAdd) {
					Topic topic = topicRepo.findObjectById(id);
					topic.addApplConfigurations(savedConfiguration);
					savedConfiguration.addTopic(topic);
				}
				
				for (Long id : topicsToRemove) {
					Topic topic = topicRepo.findObjectById(id);
					topic.getApplConfigurations().remove(savedConfiguration);
					savedConfiguration.getTopics().remove(topic);
				}
				
				savedConfiguration = configRepo.saveAndFlush(savedConfiguration);	
			}
		} catch (Exception e) {
			log.error("Error updating the configuration!", e);
			return new ResponseEntity<Configuration>(savedConfiguration, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("updateConfiguration-->");
		return new ResponseEntity<Configuration>(savedConfiguration, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeConfiguration", nickname = "removeConfiguration")
	@RequestMapping(value = "/AdminService/removeConfiguration/{id}", method = RequestMethod.DELETE)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the configuration id that should be removed", required = true, dataType = "Long", paramType = "path")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<String> removeConfiguration(@PathVariable Long id) {
		log.info("--> removeConfiguration");
		
		Configuration savedConfiguration = null;
		try {
			savedConfiguration = configRepo.findObjectById(id);
			if (savedConfiguration != null) {
				
				for (Solution solution : savedConfiguration.getSolutions()) {
					solution.getApplSolConfigurations().remove(savedConfiguration);
				}
				savedConfiguration.setSolutions(new ArrayList<Solution>());
				for (Topic topic : savedConfiguration.getTopics()) {
					topic.getApplConfigurations().remove(savedConfiguration);
				}
				savedConfiguration.setTopics(new ArrayList<Topic>());
				
				savedConfiguration = configRepo.saveAndFlush(savedConfiguration);
				configRepo.delete(id);
			}
			
		} catch (Exception e) {
			return new ResponseEntity<String>("Error deleting the Configuration from the DB!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("removeConfiguration -->");
		return new ResponseEntity<String>("Solution removed!", HttpStatus.OK);
	}

}
