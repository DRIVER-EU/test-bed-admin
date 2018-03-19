package eu.driver.admin.service.filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import eu.driver.admin.service.dto.log.Log;


@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {
	
	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(Log.class);
	}
}