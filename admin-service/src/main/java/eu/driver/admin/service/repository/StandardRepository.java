package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.standard.Standard;

@RepositoryRestResource
public interface StandardRepository  extends JpaRepository<Standard, Long> {

	public final static String ID_QUERY = "SELECT u FROM Standard u where u.id=:objectId";
	
	public final static String NAME_QUERY = "SELECT u FROM Standard u where u.name=:name";
	
	@Query(ID_QUERY)
    public Standard findObjectById(@Param("objectId") Long objectId);
	
	@Query(NAME_QUERY)
    public Standard findObjectByName(@Param("name") String name);
}
