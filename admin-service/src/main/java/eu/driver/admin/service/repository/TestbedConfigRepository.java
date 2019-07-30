package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.configuration.TestbedConfig;

@RepositoryRestResource
public interface TestbedConfigRepository extends JpaRepository<TestbedConfig, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM TestbedConfig u where u.id=:objectId";
	public final static String ACTIVE_QUERY = "SELECT u FROM TestbedConfig u where u.isActive=:active";
	
	@Query(ID_QUERY)
    public TestbedConfig findObjectById(@Param("objectId") Long objectId);
	
	@Query(ACTIVE_QUERY)
    public TestbedConfig findActiveConfig(@Param("active") Boolean active);
}
