package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.configuration.Configuration;

@RepositoryRestResource
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
	public final static String ID_QUERY = "SELECT u FROM Configuration u where u.id=:objectId";
	
	@Query(ID_QUERY)
    public Configuration findObjectById(@Param("objectId") Long objectId);
}
