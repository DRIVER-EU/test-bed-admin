package eu.driver.admin.service.filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import eu.driver.admin.service.dto.log.Log;


@Configuration
public class RepositoryConfig extends RepositoryRestMvcConfiguration {
	protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(Log.class);
	}
}
