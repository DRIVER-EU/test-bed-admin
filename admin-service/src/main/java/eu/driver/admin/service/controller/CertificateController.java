package eu.driver.admin.service.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;

import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.helper.CertHTTPUtils;


@RestController
public class CertificateController {

	private Logger log = Logger.getLogger(this.getClass());
	private ClientProperties clientProp = ClientProperties.getInstance();
	
	private CertHTTPUtils httpUtils = null;
	private String superUserPwd = null;
	private String certHostUrl = null;
	private String getPEMCertificateHOST = null;
	
	private String fileStorageLocation = "./config/cert/";
	
	public CertificateController() {
		certHostUrl = clientProp.getProperty("cert.handler.url", "https://localhost:8443/ejbca/ejbca-rest-api/v1/ees");
		getPEMCertificateHOST = clientProp.getProperty("cert.pem.handler.url", "https://localhost:8443/ejbca/ejbca-rest-api/v1/certificate/enrollkeystore");
		if (System.getenv().get("cert_handler_url") != null) {
			certHostUrl = certHostUrl.replace("https://localhost:8443", System.getenv().get("cert_handler_url"));
			getPEMCertificateHOST = getPEMCertificateHOST.replace("https://localhost:8443", System.getenv().get("cert_handler_url"));
		}
	}
	
	@ApiOperation(value = "createSolutionCertificate", nickname = "createSolutionCertificate")
	@RequestMapping(value = "/AdminService/createSolutionCertificate", method = RequestMethod.GET)
	@ApiImplicitParams({
        @ApiImplicitParam(name = "clientID", value = "the clientID of the soultion", required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "oranisationName", value = "the oranisationName of the soultion", required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "password", value = "the password of the certificate", required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "email", value = "the contact email of the soultion", required = false, dataType = "string", paramType = "query")
      })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = String.class),
			@ApiResponse(code = 500, message = "Failure", response = String.class) })
	public ResponseEntity<String> createSolutionCertificate(
			@QueryParam("clientID") String clientID, 
			@QueryParam("oranisationName") String oranisationName, 
			@QueryParam("password") String password, 
			@QueryParam("email") String email) {
		log.info("--> createSolutionCertificate");
		
		httpUtils = new CertHTTPUtils(superUserPwd);
		
		JSONObject certRequest = new JSONObject();
		try {
			certRequest.put("certificate_profile_name", "ENDUSER");
			certRequest.put("end_entity_profile_name", "TLS_EE");
			certRequest.put("ca_name", "Issuing_CA");
			certRequest.put("token_type", "P12");
			certRequest.put("username", clientID);
			certRequest.put("password", password);
			certRequest.put("email", email);
			certRequest.put("subject_dn","CN=" + clientID + ",O=" + oranisationName);
												
		} catch (JSONException jEx) {
			log.error("Error creating the JSON certificate request structure!");
			return new ResponseEntity<String>("The Certificate could not be created!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try {
			httpUtils.postHTTPRequest(certHostUrl, "POST", "application/json", certRequest.toString());
		} catch (CommunicationException cex) {
			log.error("Error creating the certificate!", cex);
			return new ResponseEntity<String>("The Certificate could not be created!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		JSONObject getCertRequest = new JSONObject();
		try {
			getCertRequest.put("username",clientID);
			getCertRequest.put("password",password);
			getCertRequest.put("key_alg","RSA");
			getCertRequest.put("key_spec","2048");
		} catch (JSONException jEx) {
			log.error("Error creating the JSON get certificate request structure!");
			return new ResponseEntity<String>("The Certificate could not be created!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		String certJsonString = null;
		
		try {
			certJsonString = httpUtils.postHTTPRequest(getPEMCertificateHOST, "POST", "application/json", getCertRequest.toString());
		} catch (CommunicationException cex) {
			log.error("Error getting the certificate!", cex);
			return new ResponseEntity<String>("The Certificate could not be created!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		String fileName = "";
		
		if (certJsonString != null) {
			try {
				JSONObject certJson = new JSONObject(certJsonString);
				String keystoreData = certJson.getString("keystore_data");
				byte[] decoded = Base64.getDecoder().decode(keystoreData);
				byte[] decoded2 = Base64.getMimeDecoder().decode(new String(decoded, StandardCharsets.UTF_8));
				fileName = clientID + ".p12";
				Path file = Paths.get(this.fileStorageLocation + fileName);
				Files.write(file, decoded2);
			} catch (JSONException jEx) {
				log.error("Error parsing the CERT Json response!");
				return new ResponseEntity<String>("The Certificate could not be created!", HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				log.error("Error creating the p12 file!");
				return new ResponseEntity<String>("The Certificate could not be created!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		log.info("createSolutionCertificate -->");
		return new ResponseEntity<String>(fileName, HttpStatus.OK);
	}
	
	@RequestMapping(value="/AdminService/downloadCertificate/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		Resource resource = null;
		if (!fileName.endsWith(".p12")) {
			fileName += ".p12";
		}
		try {
			Path fileStoragePath= Paths.get(this.fileStorageLocation).toAbsolutePath().normalize();
			Path filePath = fileStoragePath.resolve(fileName).normalize();
            resource = new UrlResource(filePath.toUri());
		} catch (Exception e) {
			log.error("Error loading the file: " + fileName);
		}
		if (resource != null) {
			String contentType = null;
	        try {
	            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	        } catch (IOException ex) {
	            log.info("Could not determine file type.");
	        }

	        // Fallback to the default content type if type could not be determined
	        if(contentType == null) {
	            contentType = "application/octet-stream";
	        }

	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(contentType))
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	                .body(resource);
		}

		return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
    }

	public String getSuperUserPwd() {
		return superUserPwd;
	}

	public void setSuperUserPwd(String superUserPwd) {
		this.superUserPwd = superUserPwd;
	}
}
