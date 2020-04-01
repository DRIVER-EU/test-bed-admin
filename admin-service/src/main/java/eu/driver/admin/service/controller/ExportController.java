package eu.driver.admin.service.controller;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.organisation.Organisation;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.topic.Topic;
import eu.driver.admin.service.helper.FileStorageService;
import eu.driver.admin.service.repository.ConfigurationRepository;
import eu.driver.admin.service.repository.GatewayRepository;
import eu.driver.admin.service.repository.LogRepository;
import eu.driver.admin.service.repository.OrganisationRepository;
import eu.driver.admin.service.repository.SolutionRepository;
import eu.driver.admin.service.repository.StandardRepository;
import eu.driver.admin.service.repository.TestbedConfigRepository;
import eu.driver.admin.service.repository.TopicRepository;

@RestController
public class ExportController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	OrganisationRepository orgRepo;
	
	@Autowired
	SolutionRepository solutionRepo;
	
	@Autowired
	GatewayRepository gatewayRepo;
	
	@Autowired
	TopicRepository topicRepo;
	
	@Autowired
	StandardRepository standardRepo;
	
	@Autowired
	LogRepository logRepo;
	
	@Autowired
	ConfigurationRepository configRepo;
	
	@Autowired
	TestbedConfigRepository testbedConfigRepo;
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@Autowired
	private MgmtController mgmtController;
	
	private String organisationJson = "./admintoolconfig/organisations.json";
	
	private String configJson = "./admintoolconfig/configurations.json";
	
	private String solutionJson= "./admintoolconfig/solutions.json";
	private String topicJson= "./admintoolconfig/topics.json";
	
	private String tbSolutionJson = "./admintoolconfig/testbed-solutions.json";
	private String tbTopicJson = "./admintoolconfig/testbed-topics.json";
	
	
	public ExportController() {
		log.info("--> ExportController");
		if (!(Files.exists(Paths.get("./admintoolconfig")))) {
            try {
				Files.createDirectories(Paths.get("./admintoolconfig"));
			} catch (IOException e) {
				log.error("Error creating the ./record directory!");
			}
        }
		log.info("ExportController-->");
	}
	
	@ApiOperation(value = "exportConfigurationData", nickname = "exportConfigurationData")
	@RequestMapping(value = "/AdminService/exportConfigurationData", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = String.class),
			@ApiResponse(code = 500, message = "Failure", response = String.class) })
	public ResponseEntity<byte[]> exportConfigurationData() {
		log.info("--> exportConfigurationData");
		
		File zipFile = new File("adminconfig.zip");
		if (zipFile.exists()) {
			zipFile.delete();
		}
		byte[] fileContent = null;
		
		List<Organisation> orgList = orgRepo.findAll();
		
		List<Solution> solutionList = solutionRepo.findAll();
		List<Topic> topicList = topicRepo.findAll();
		
		List<Solution> tbsolutionList = new ArrayList<Solution>();
		List<Topic> tbtopicList = new ArrayList<Topic>();
		
		List<Solution> extsolutionList = new ArrayList<Solution>();
		List<Topic> exttopicList = new ArrayList<Topic>();
		
		List<Configuration> configurationList = configRepo.findAll();
		
		for (Solution solution : solutionList) {
			if (solution.getIsService()) {
				tbsolutionList.add(solution);
			} else {
				extsolutionList.add(solution);
			}
		}
		
		for (Topic topic : topicList) {
			if (topic.getType().equalsIgnoreCase("core.topic")) {
				tbtopicList.add(topic);
			} else {
				exttopicList.add(topic);
			}
		}
		try {
			String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(orgList);
			Path path = Paths.get(organisationJson);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) 
			{
			    writer.write(json);
			    writer.close();
			}
		
			json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configurationList);
			path = Paths.get(configJson);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) 
			{
			    writer.write(json);
			    writer.close();
			}
			
			json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tbsolutionList);
			path = Paths.get(tbSolutionJson);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) 
			{
			    writer.write(json);
			    writer.close();
			}
			
			json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(extsolutionList);
			path = Paths.get(solutionJson);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) 
			{
			    writer.write(json);
			    writer.close();
			}
			
			json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tbtopicList);
			path = Paths.get(tbTopicJson);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) 
			{
			    writer.write(json);
			    writer.close();
			}
			
			json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exttopicList);
			path = Paths.get(topicJson);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) 
			{
			    writer.write(json);
			    writer.close();
			}
		} catch(Exception e) {
			log.error("Error creating organisation json file!");
		}
		
		try {
			this.pack("./admintoolconfig",  "adminconfig.zip");
			File file = new File("adminconfig.zip");
			
			try {
				fileContent = Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				log.error("Error loading the file!", e);
			}
		} catch (IOException ie) {
			log.error("error creating the zip file");
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/zip"));
		headers.setContentDispositionFormData("attachment", "adminconfig.zip"); 
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    log.info("exportConfigurationData-->");
	    return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
	}
	
	@ApiOperation(value = "uploadConfigurationData", nickname = "uploadConfigurationData")
	@RequestMapping(value = "/AdminService/uploadConfigurationData", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "file", value = "the file to be uploaded", required = true, dataType = "__file")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Transactional
	public ResponseEntity<String> uploadConfigurationData(@RequestPart("file") MultipartFile file) {
		log.info("-->uploadConfigurationData");
		
		String fileName = fileStorageService.storeFile("./config", file);
		
		if (fileName != null) {
			try {
				this.unzip(fileName, "./config");
			} catch (IOException e) {
				log.error("Error unzipping the backup!", e);
			}
		}
		
		mgmtController.loadInitData(true);
		
		log.info("uploadConfigurationData-->");
		return new ResponseEntity<String>(fileName, HttpStatus.OK);
	}
	
	private void pack(String sourceDirPath, String zipFilePath) throws IOException {
	    Path p = Files.createFile(Paths.get(zipFilePath));
	    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
	        Path pp = Paths.get(sourceDirPath);
	        Files.walk(pp)
	          .filter(path -> !Files.isDirectory(path))
	          .forEach(path -> {
	              ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
	              try {
	                  zs.putNextEntry(zipEntry);
	                  Files.copy(path, zs);
	                  zs.closeEntry();
	            } catch (IOException e) {
	                System.err.println(e);
	            }
	          });
	    }
	}
	
	private void unzip(final String zipFilePath, final String unzipLocation) throws IOException {

        if (!(Files.exists(Paths.get(unzipLocation)))) {
            Files.createDirectories(Paths.get(unzipLocation));
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path filePath = Paths.get(unzipLocation, entry.getName());
                if (!entry.isDirectory()) {
                	// create the DIR structure
                	String strPath = filePath.toString();
                	int idx = strPath.lastIndexOf("\\");
                	if (idx < 0) {
                		idx = strPath.lastIndexOf("/");
                	}
                	Files.createDirectories(Paths.get(strPath.substring(0, idx)));
                	File file = new File(strPath);
                	file.createNewFile();
                    unzipFiles(zipInputStream, filePath);
                } else {
                    Files.createDirectories(filePath);
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }

	private void unzipFiles(final ZipInputStream zipInputStream, final Path unzipFilePath) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipFilePath.toAbsolutePath().toString(), false))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }

    }

}
