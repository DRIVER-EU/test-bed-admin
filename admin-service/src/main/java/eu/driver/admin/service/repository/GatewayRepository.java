package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.gateway.Gateway;

@RepositoryRestResource
public interface GatewayRepository extends JpaRepository<Gateway, Long> {
	public final static String ID_QUERY = "SELECT u FROM Gateway u where u.id=:objectId";
	
	public final static String CLIENT_ID_QUERY = "SELECT u FROM Gateway u where u.clientId=:clientId";
	
	@Query(ID_QUERY)
    public Gateway findObjectById(@Param("objectId") Long objectId);
	
	@Query(CLIENT_ID_QUERY)
    public Gateway findObjectByClientId(@Param("clientId") String clientId);
}
