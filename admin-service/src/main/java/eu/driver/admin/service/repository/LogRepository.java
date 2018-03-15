package eu.driver.admin.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.log.Log;


@RepositoryRestResource
public interface LogRepository extends JpaRepository<Log, Long> {
		
		public final static String ID_QUERY = "SELECT u FROM Log u where u.id=:objectId";
		
		@Query(ID_QUERY)
	    public Log findObjectById(@Param("objectId") Long objectId);
	}




