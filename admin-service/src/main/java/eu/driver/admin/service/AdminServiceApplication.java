package eu.driver.admin.service;

import static springfox.documentation.builders.PathSelectors.regex;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.constants.TestbedSecurityMode;
import eu.driver.admin.service.controller.CertificateController;
import eu.driver.admin.service.controller.LogRESTController;
import eu.driver.admin.service.controller.MgmtController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@EnableSwagger2
@SpringBootApplication
public class AdminServiceApplication {

	private Logger log = Logger.getLogger(this.getClass());
	private String superUserPwd = null;
	private ClientProperties clientProp = ClientProperties.getInstance();
	private TestbedSecurityMode secureMode = TestbedSecurityMode.DEVELOP;
	private String managementCAPath = null;
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	MgmtController mgmtController;

	@Autowired
	CertificateController certController;
	
	public AdminServiceApplication() throws Exception {
		log.info("Init. AdminServiceApplication");
		secureMode = TestbedSecurityMode.valueOf(clientProp.getProperty("testbed.secure.mode", "DEVELOP"));
		managementCAPath = clientProp.getProperty("management.ca.cert.path");
		if (System.getenv().get("testbed_secure_mode") != null) {
			secureMode = TestbedSecurityMode.valueOf(System.getenv().get("testbed_secure_mode"));
		}
		
		superUserPwd = clientProp.getProperty("superadmin.password");
		
		Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
        	if (envName.equalsIgnoreCase("zookeeper_host")) {
        		log.info(envName + ": " + env.get(envName));
        	} else if (envName.equalsIgnoreCase("zookeeper_port")) {
        		log.info(envName + ": " + env.get(envName));
        	} else if (envName.equalsIgnoreCase("schema_registry_url")) {
        		log.info(envName + ": " + env.get(envName));
        	} else if (envName.equalsIgnoreCase("testbed_init_auto")) {
        		log.info(envName + ": " + env.get(envName));
        	} else if (envName.equalsIgnoreCase("management_ca_cert_path")) {
        		log.info(envName + ": " + env.get(envName));
        		managementCAPath = managementCAPath.replace("http://localhost:9090", env.get(envName));
        	} else if (envName.equalsIgnoreCase("cert_handler_url")) {
        		log.info(envName + ": " + env.get(envName));
        	} else if (envName.equalsIgnoreCase("security_rest_path_group")) {
        		log.info(envName + ": " + env.get(envName));
        	} else if (envName.equalsIgnoreCase("security_rest_path_topic")) {
        		log.info(envName + ": " + env.get(envName));
        	}
        }
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(AdminServiceApplication.class, args);
    }
	
	@PostConstruct
	public void init() {
		if ((secureMode.equals(TestbedSecurityMode.AUTHENTICATION) || secureMode.equals(TestbedSecurityMode.AUTHENTICATION_AND_AUTHORIZATION)) && managementCAPath != null) {
			this.getManagementCA();
			this.certController.setSuperUserPwd(superUserPwd);
		}
		boolean resetDB = Boolean.parseBoolean(ClientProperties.getInstance().getProperty("reset.db"));
		if (System.getenv().get("reset_db") != null) {
			resetDB = Boolean.parseBoolean(System.getenv().get("reset_db"));
		} 
		mgmtController.loadInitData(resetDB);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "The AdminService is up!", true);
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "Testbed is running in: " + secureMode + " mode.", true);
		
		boolean initAuto = Boolean.parseBoolean(ClientProperties.getInstance().getProperty("init.auto"));
		if (System.getenv().get("testbed_init_auto") != null) {
			initAuto = Boolean.parseBoolean(System.getenv().get("testbed_init_auto"));
		} 
		if(initAuto) {
			mgmtController.initTestbed();
		}
	}
	
	private void getAdminToolCertificate() {
		try {
	        String[] crtCommand = {"docker", "cp", "localsecurity_cert_mgt_1:/opt/pki/ejbca/p12/superadmin.p12", "./config/cert"};
	        ProcessBuilder crtPb = new ProcessBuilder(crtCommand);
	        crtPb.inheritIO();
	        Process crtProc = crtPb.start();
	        
	        crtProc.waitFor();
	        
	        String[] pwdCommand = {"docker", "exec", "localsecurity_cert_mgt_1", "grep", "superadmin.password", "/opt/pki/ejbca/conf/web.properties"};
	        ProcessBuilder pwdPb = new ProcessBuilder(pwdCommand);
	        //pwdPb.inheritIO();
	        pwdPb.redirectErrorStream(true);
	        Process pwdProc = pwdPb.start();

	        BufferedReader reader = new BufferedReader(new InputStreamReader(pwdProc.getInputStream()));
	        
	        String line = "";
	        int value = 0;
			while((value = reader.read()) != -1) {
			   char c = (char)value;
			   line += c;
			}
	         
	        if (line != "") {
	        	StringTokenizer token = new StringTokenizer(line, "=");
	        	superUserPwd = token.nextToken();
	        	superUserPwd = token.nextToken();
	        }
	        reader.close();
	        pwdProc.waitFor();
	        superUserPwd = superUserPwd.replace("\n", "");
	        log.info("SuperUserPassword = " + superUserPwd);
	        this.certController.setSuperUserPwd(superUserPwd);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private  void getManagementCA() {
		String fileName = "config/cert/ManagementCA-chain.jks";
		InputStream in;
		boolean done = false;
		while(!done) {
			try {
				in = new URL(managementCAPath).openStream();
				Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
				done = true;
			} catch (IOException e) {
				log.info("Waiting for " + managementCAPath + " is up!");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					
				}
			}
		}
		
		
	    try {
	    	KeyStore origKs = loadKeyStore( "config/cert/ManagementCA-chain.jks", "changeit");
	    	KeyStore mgmtKs = loadKeyStore( "config/cert/truststore-base.jks", "changeit");
	    	mergeKeystores(origKs, mgmtKs);
	    	this.writeKeyStore(origKs, "config/cert/ManagementCA-chain.jks", "changeit");
	    	
	    	System.setProperty("javax.net.ssl.trustStore", "config/cert/ManagementCA-chain.jks");
			System.setProperty("javax.net.ssl.trustStoreType","JCEKS");
			System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
	    	
	    } catch (Exception e) {
	    	log.error("Error removing the ManagementCA from KeyStore!");
	    }
	}
	
	private void mergeKeystores(KeyStore newKeystore, KeyStore oldKeystore) throws Exception{
		// Get all aliases in the old keystore
		Enumeration enumeration = oldKeystore.aliases();
		while(enumeration.hasMoreElements()) {
		  // Determine the current alias
		  String alias = (String)enumeration.nextElement();
		  // Get Key & Certificates
		  Key key = oldKeystore.getKey(alias, "changeit".toCharArray());
		  Certificate[] certs = oldKeystore.getCertificateChain(alias);
		  // Put them altogether in the new keystore
		  newKeystore.setKeyEntry(alias, key, "changeit".toCharArray(), certs);
		}
	}
	
	private KeyStore loadKeyStore(String storePath, String storePass) throws Exception {
		KeyStore ks = KeyStore.getInstance("JCEKS");

	    // get user password and file input stream
	    char[] password = storePass.toCharArray();

	    java.io.FileInputStream fis = null;
	    try {
	        fis = new java.io.FileInputStream(storePath);
	        ks.load(fis, password);
	    } finally {
	        if (fis != null) {
	            fis.close();
	        }
	    }
	    return ks;
	}
	
	private void writeKeyStore(KeyStore ks, String storePath, String storePass) throws Exception {
		OutputStream writeStream = null;
		try {
			writeStream = new FileOutputStream(storePath);
			ks.store(writeStream, storePass.toCharArray());
		} catch (Exception e) {
			
		} finally {
			if (writeStream != null) {
				writeStream.close();
	        }
		}
	}
	
	@Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("AdminService")
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/AdminService.*"))
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("AdminServiceApplication REST Interface API Doc.")
                .description("This is the AdminServiceApplication REST Interface API Documentation made with Swagger.")
                .version("1.0")
                .build();
    }

	public MgmtController getMgmtController() {
		return mgmtController;
	}

	public void setMgmtController(MgmtController mgmtController) {
		this.mgmtController = mgmtController;
	}

	public LogRESTController getLogController() {
		return logController;
	}

	public void setLogController(LogRESTController logController) {
		this.logController = logController;
	}

	public CertificateController getCertController() {
		return certController;
	}

	public void setCertController(CertificateController certController) {
		this.certController = certController;
	}
	
}
