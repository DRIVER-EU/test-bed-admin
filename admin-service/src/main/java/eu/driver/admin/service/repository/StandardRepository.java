package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.standard.Standard;

@RepositoryRestResource
public interface StandardRepository  extends JpaRepository<Standard, Long> {

	public final static String ID_QUERY = "SELECT u FROM Standard u where u.id=:objectId ORDER BY u.name DESC";
	
	public final static String NAME_AND_NAMESPACE_QUERY = "SELECT u FROM Standard u where u.name=:name and u.namespace=:namespace";
	
	@Query(ID_QUERY)
    public Standard findObjectById(@Param("objectId") Long objectId);
	
	@Query(NAME_AND_NAMESPACE_QUERY)
    public Standard findObjectByNameAndNamespace(@Param("name") String name, @Param("namespace") String namespace);
}
