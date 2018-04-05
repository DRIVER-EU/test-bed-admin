package eu.driver.admin.service;

import static springfox.documentation.builders.PathSelectors.regex;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import eu.driver.adapter.properties.ClientProperties;
import eu.driver.admin.service.constants.LogLevels;
import eu.driver.admin.service.controller.LogRESTController;
import eu.driver.admin.service.controller.MgmtController;
import eu.driver.model.core.Level;
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
	
	@Autowired
	LogRESTController logController;
	
	@Autowired
	MgmtController mgmtController;
	
	public AdminServiceApplication() throws Exception {
		log.info("Init. AdminServiceApplication");
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(AdminServiceApplication.class, args);
    }
	
	@PostConstruct
	public void init() {
		logController.addLog(LogLevels.LOG_LEVEL_INFO, "The AdminService is up!", true);
		if(Boolean.parseBoolean(ClientProperties.getInstance().getProperty("init.auto"))) {
			mgmtController.initTestbed();
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
}
