package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.organisation.Organisation;

@RepositoryRestResource
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Organisation u where u.id=:objectId ORDER BY u.id DESC";
	
	public final static String ORG_NAME_QUERY = "SELECT u FROM Organisation u where u.orgName=:orgName";
	
	@Query(ID_QUERY)
    public Organisation findObjectById(@Param("objectId") Long objectId);
	
	@Query(ORG_NAME_QUERY)
    public Organisation findObjectByOrgName(@Param("orgName") String orgName);
}
