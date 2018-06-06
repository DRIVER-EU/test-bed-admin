package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.solution.Solution;

@RepositoryRestResource
public interface SolutionRepository extends JpaRepository<Solution, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Solution u where u.id=:objectId";
	
	public final static String CLIENT_ID_QUERY = "SELECT u FROM Solution u where u.clientId=:clientId";
	
	@Query(ID_QUERY)
    public Solution findObjectById(@Param("objectId") Long objectId);
	
	@Query(CLIENT_ID_QUERY)
    public Solution findObjectByClientId(@Param("clientId") String clientId);
}
