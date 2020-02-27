package eu.driver.admin.service.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.driver.admin.service.dto.standard.Standard;
import eu.driver.admin.service.helper.FileStorageService;
import eu.driver.admin.service.repository.StandardRepository;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

@RestController
public class SchemaController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@Autowired
	StandardRepository standardRepo;
	
	public SchemaController() {
		
	}
	
	
	@ApiOperation(value = "uploadSchema", nickname = "uploadSchema")
	@RequestMapping(value = "/AdminService/uploadSchema", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "file", value = "the file to be uploaded", required = true, dataType = "__file")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 409, message = "Conflict", response = Boolean.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = Boolean.class) })
	@Transactional
	public ResponseEntity<Standard> uploadSchema(@RequestPart("file") MultipartFile file) {
		log.info("-->uploadSchema");
		String fileName = fileStorageService.storeFile("./config/schema/uploaded", file);
		Standard savedStandard = null;
		
		try {
			Parser parser = new Parser();
		    Schema mSchema = parser.parse(new File(fileName));
		    String version = mSchema.getProp("version");
		    if (version == null) {
		    	version = "1.0";
		    }
		    Standard standard = new Standard();
		    standard.setName(mSchema.getName());
		    standard.setNamespace(mSchema.getNamespace());
		    List<String> versions = new ArrayList<String>();
		    versions.add(version);
		    standard.setVersions(versions);
		    standard.setFileName(fileName);
		    
		    savedStandard = standardRepo.findObjectByNameAndNamespace(standard.getName(), standard.getNamespace());
		    if (savedStandard != null) {
		    	return new ResponseEntity<Standard>(savedStandard, HttpStatus.CONFLICT);
		    }
		    savedStandard = standardRepo.saveAndFlush(standard);
		} catch (Exception e) {
			log.error("Error uploading the schema!", e);
			return new ResponseEntity<Standard>(savedStandard, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("uploadSchema-->");
		return new ResponseEntity<Standard>(savedStandard, HttpStatus.OK);
	}

}
