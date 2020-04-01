package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.core.AdminAdapter;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.controller.heartbeat.SolutionHeartbeatTimerTask;
import eu.driver.admin.service.dto.SolutionList;
import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.configuration.TestbedConfig;
import eu.driver.admin.service.dto.log.Log;
import eu.driver.admin.service.dto.solution.Origin;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.OrganisationRepository;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.repository.TestbedConfigRepository;
import eu.driver.admin.service.ws.WSController;
import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSSolutionStateChange;
import eu.driver.api.IAdaptorCallback;
import eu.driver.model.core.Level;

@RestController
public class SolutionRESTController {
	private Logger log = Logger.getLogger(this.getClass());
	
	private Timer hbTimer = null;
	private SolutionHeartbeatTimerTask hbTimerTask = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	
	private String clientId = ClientProperties.getInstance().getProperty("client.id");
	
	private String fileStorageLocation = "./config/cert/";
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	SolutionRepository solutionRepo;
	
	@Autowired
	ConfigurationRepository configRepo;
	
	@Autowired
	TestbedConfigRepository testbedConfigRepo;
	
	@Autowired
	OrganisationRepository orgRepo;
	
	public MgmtController mgmtController;
	
	private Origin origin = new Origin();

	public SolutionRESTController() {
		log.info("-->SolutionRESTController");
		
		hbTimerTask = new SolutionHeartbeatTimerTask(this);
		hbTimer = new Timer();
		hbTimer.schedule(hbTimerTask, 3000, 3000);
		
		InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            if (ip != null && ip.getHostName() != null) {
            	origin.setHosteName(ip.getHostName());
            }
            if (ip != null) {
            	origin.setLocalIp(ip.getHostAddress());
            }
            
            String remoteIP = this.getRemoteIP();
            if (remoteIP != null) {
            	origin.setRemoteIp(remoteIP);
            }
        } catch (UnknownHostException e) {
        	log.error("Error optaining IP and Hostname!");
        }
		log.info("-->SolutionRESTController");
	}
	
	public void updateSolutionState(String clientID, Date isAliveDate, String origin, Boolean state) {
		log.debug("-->updateSolutionState");
		try {
			Solution solution = this.solutionRepo.findObjectByClientId(clientID);
			if (solution != null) {
				solution.setLastHeartBeatReceived(new Date());
				Origin solOrigin = solution.getOrigin();
				if (solOrigin != null) {
					solOrigin = new Origin();
				}
				if (origin != null) {
					JSONObject jsonOrigin = new JSONObject(origin);
					solOrigin.setLocalIp(jsonOrigin.getString("local_IP"));
					solOrigin.setHosteName(jsonOrigin.getString("hostname"));
					solOrigin.setRemoteIp(jsonOrigin.getString("remote_IP"));
					solution.setOrigin(solOrigin);
				}
				if (solution.getState() != state) {
					solution.setState(state);
					eu.driver.model.core.Log logEntry = new eu.driver.model.core.Log();
					logEntry.setId(clientId);
					logEntry.setDateTimeSent(new Date().getTime());
					if (state) {
						logEntry.setLevel(Level.INFO);
					} else {
						logEntry.setLevel(Level.ERROR);
					}
					logEntry.setLog("The Solution: " + solution.getName() + " changed its state to: " + solution.getState());
					AdminAdapter.getInstance().addLogEntry(logEntry);
					WSSolutionStateChange notification = new WSSolutionStateChange(solution.getClientId(), solution.getState());
					WSController.getInstance().sendMessage(mapper.objectToJSONString(notification));
					
					if (solution.getState()) {
						// send topic invites if solution state is up (again)
						if(mgmtController != null && solution.getClientId() != null) {
							mgmtController.sendTopicInvitesForClient(solution.getClientId());	
						}
					}
				}
				this.solutionRepo.saveAndFlush(solution);
			}
		} catch (Exception e) {
			log.error("Error updating the solution!" , e);
		}
		log.debug("updateSolutionState-->");	
	}
	
	@ApiOperation(value = "addSolution", nickname = "addSolution")
	@RequestMapping(value = "/AdminService/addSolution", method = RequestMethod.POST)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "solution", value = "the solution that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Solution> addSolution(@RequestBody Solution solution) {
		log.debug("--> addSolution");
		Solution savedSolution = null;
		
		try {
			savedSolution = solutionRepo.saveAndFlush(solution);
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Solution: " + solution.getName() + " has been created!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Solution>(savedSolution, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> addSolution");
		return new ResponseEntity<Solution>(savedSolution, HttpStatus.OK);
	}
	
	@ApiOperation(value = "updateSolution", nickname = "updateSolution")
	@RequestMapping(value = "/AdminService/updateSolution", method = RequestMethod.PUT)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "solution", value = "the solution that should be saved", required = true, dataType = "application/json", paramType = "body")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<Solution> updateSolution(@RequestBody Solution solution) {
		log.debug("--> updateSolution");
		Solution savedSolution = null;
		
		savedSolution = solutionRepo.findObjectById(solution.getId());
		
		try {
			if (savedSolution != null) {
				savedSolution.setDescription(solution.getDescription());
				savedSolution.setClientId(solution.getClientId());
				savedSolution.setIsAdmin(solution.getIsAdmin());
				savedSolution.setIsService(solution.getIsService());
				savedSolution.setName(solution.getName());
				savedSolution.setState(solution.getState());
				savedSolution.setLastHeartBeatReceived(solution.getLastHeartBeatReceived());
				
				if (solution.getOrganisation() != null) {
					savedSolution.setOrganisation(solution.getOrganisation());
				}
				
				if (savedSolution.getOrganisation().getOrgName() != null && savedSolution.getName() != null) {
					savedSolution.setSubjectId(
							"O="+savedSolution.getOrganisation().getOrgName().toUpperCase() 
							+ ",CN=" + savedSolution.getName().toUpperCase().replaceAll(" ", "-"));
				}
				
				if (solution.getApplSolConfigurations() != null) {
					if (savedSolution.getApplSolConfigurations().size() != solution.getApplSolConfigurations().size()) {
						savedSolution.setApplSolConfigurations(solution.getApplSolConfigurations());	
					}	
				}
				
				savedSolution = solutionRepo.saveAndFlush(savedSolution);
			} else {
				if (solution.getOrganisation().getOrgName() != null && solution.getName() != null) {
					solution.setSubjectId(
							"O="+solution.getOrganisation().getOrgName().toUpperCase() 
							+ ",CN=" + solution.getName().toUpperCase().replaceAll(" ", "-"));
				}
				savedSolution = solutionRepo.saveAndFlush(solution);
			}
			if (logController != null) {
				logController.addLog(LogLevels.LOG_LEVEL_INFO,
					"The Solution: " + solution.getName() + " has been updated!", true);
			}
		} catch (Exception e) {
			return new ResponseEntity<Solution>(savedSolution, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("--> updateSolution");
		return new ResponseEntity<Solution>(savedSolution, HttpStatus.OK);
	}
	
	@ApiOperation(value = "removeSolution", nickname = "removeSolution")
	@RequestMapping(value = "/AdminService/removeSolution/{id}", method = RequestMethod.DELETE)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "the solution id that should be removed", required = true, dataType = "Long", paramType = "path")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Solution.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Solution.class),
			@ApiResponse(code = 500, message = "Failure", response = Solution.class) })
	public ResponseEntity<String> removeSolution(@PathVariable Long id) {
		log.debug("--> removeSolution");
		
		try {
			solutionRepo.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<String>("Error deleting the Solution from the DB!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.debug("removeSolution -->");
		return new ResponseEntity<String>("Solution removed!", HttpStatus.OK);
	}
	
	public Boolean removeAllSolutions() {
		log.debug("--> removeAllSolutions");
		try {
			solutionRepo.deleteAll();
		} catch (Exception e) {
			log.error("Error removing all Solutions!", e);
			return false;
		}
		log.debug("removeSolution -->");
		return true;
	}
	
	@ApiOperation(value = "getAllSolutions", nickname = "getAllSolutions")
	@RequestMapping(value = "/AdminService/getAllSolutions", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = SolutionList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = SolutionList.class),
			@ApiResponse(code = 500, message = "Failure", response = SolutionList.class) })
	public ResponseEntity<SolutionList> getAllSolutions() {
		log.info("--> getAllSolutions");
		SolutionList solutionList = new SolutionList();
		
		List<Solution> solutions = this.solutionRepo.findAll();
		try {
			Collections.sort(solutions, (a, b) -> a.getId() < b.getId() ? -1 : 0);
			solutionList.setSolutions(solutions);
		} catch (Exception e) {
			return new ResponseEntity<SolutionList>(solutionList, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("getAllSolutions -->");
		return new ResponseEntity<SolutionList>(solutionList, HttpStatus.OK);
	}

	@ApiOperation(value = "getAllTrialSolutions", nickname = "getAllTrialSolutions")
	@RequestMapping(value = "/AdminService/getAllTrialSolutions", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = SolutionList.class),
			@ApiResponse(code = 400, message = "Bad Request", response = SolutionList.class),
			@ApiResponse(code = 500, message = "Failure", response = SolutionList.class) })
	public ResponseEntity<SolutionList> getAllTrialSolutions() {
		log.info("--> getAllTrialSolutions");
		SolutionList solutionList = new SolutionList();
		
		String configName = null;
		List<Solution> solutions = this.solutionRepo.findAll();
		TestbedConfig tbConfig = testbedConfigRepo.findActiveConfig(true);
		try {
			
			if (tbConfig != null) {
				configName = tbConfig.getConfigName();
				if (configName != null) {
					Configuration config = configRepo.findObjectByName(configName);
					solutions = config.getSolutions();
					Collections.sort(solutions, (a, b) -> a.getId() < b.getId() ? -1 : 0);
				}
			}
			solutionList.setSolutions(solutions);
		} catch (Exception e) {
			return new ResponseEntity<SolutionList>(solutionList, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("getAllTrialSolutions -->");
		return new ResponseEntity<SolutionList>(solutionList, HttpStatus.OK);
	}
	
	public List<Solution> getAllTrialSolutionList() {
		log.info("--> getAllTrialSolutionList");
		
		String configName = null;
		List<Solution> solutions = this.solutionRepo.findAll();
		TestbedConfig tbConfig = testbedConfigRepo.findActiveConfig(true);
		try {
			
			if (tbConfig != null) {
				configName = tbConfig.getConfigName();
				if (configName != null) {
					Configuration config = configRepo.findObjectByName(configName);
					solutions = config.getSolutions();
				}
			}
		} catch (Exception e) {
			
		}
		log.info("getAllTrialSolutionList -->");
		return solutions;
	}
	
	public List<Solution> getSolutionList() {
		log.debug("--> getSolutionList");
		
		log.debug("getSolutionList -->");
		return this.solutionRepo.findAll();
	}
	
	
	@ApiOperation(value = "getSolutionsCertMap", nickname = "getSolutionsCertMap")
	@RequestMapping(value = "/AdminService/getSolutionsCertMap", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Map.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Map.class),
			@ApiResponse(code = 500, message = "Failure", response = Map.class) })
	public ResponseEntity<Map<String, String>> getSolutionsCertMap() {
		log.info("--> getSolutionsCertMap");
		Map<String, String> certMap = new HashMap<String, String>();

		List<Solution> solutions = this.solutionRepo.findAll();
		try {
			for (Solution solution : solutions) {
				if (Files.exists(Paths.get(this.fileStorageLocation + solution.getClientId() + ".p12"))) {
					certMap.put(solution.getClientId(), solution.getClientId() + ".p12");
				}
			}
		} catch (Exception e) {
			return new ResponseEntity<Map<String, String>>(certMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("getSolutionsCertMap -->");
		return new ResponseEntity<Map<String, String>>(certMap, HttpStatus.OK);
	}
	
	private String getRemoteIP() {
		String remoteIP = null;
		
		try {
			remoteIP = getHTTPRequest("http://ipv4bot.whatismyipaddress.com", "text/html");
		} catch (Exception e) {
			remoteIP = null;
		}
		
		return remoteIP;
	}
	
	private String getHTTPRequest(String url, String contentType) throws Exception {
		StringBuffer response = new StringBuffer();
		try {
			URL obj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
	
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", contentType);
	
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			throw new Exception("Error executing the GET request!");
		}
		
		return response.toString();

	}
	
	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}

	public SolutionRepository getSolutionRepo() {
		return solutionRepo;
	}

	public void setSolutionRepo(SolutionRepository solutionRepo) {
		this.solutionRepo = solutionRepo;
	}

	public ConfigurationRepository getConfigRepo() {
		return configRepo;
	}

	public void setConfigRepo(ConfigurationRepository configRepo) {
		this.configRepo = configRepo;
	}

	public TestbedConfigRepository getTestbedConfigRepo() {
		return testbedConfigRepo;
	}

	public void setTestbedConfigRepo(TestbedConfigRepository testbedConfigRepo) {
		this.testbedConfigRepo = testbedConfigRepo;
	}
		
}
