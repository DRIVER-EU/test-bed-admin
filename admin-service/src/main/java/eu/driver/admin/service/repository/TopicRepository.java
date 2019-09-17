package eu.driver.admin.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.driver.admin.service.dto.topic.Topic;

@RepositoryRestResource
public interface TopicRepository extends JpaRepository<Topic, Long> {
	
	public final static String ID_QUERY = "SELECT u FROM Topic u where u.id=:objectId ORDER BY u.id DESC";
	
	public final static String CLIENT_ID_QUERY = "SELECT u FROM Topic u where u.clientId=:clientId";
	
	public final static String NAME_QUERY = "SELECT u FROM Topic u where u.name=:name";
	
	public final static String TYPE_QUERY = "SELECT u FROM Topic u where u.type=:type";
	
	@Query(ID_QUERY)
    public Topic findObjectById(@Param("objectId") Long objectId);
	
	@Query(CLIENT_ID_QUERY)
    public Topic findObjectByClientId(@Param("clientId") String clientId);
	
	@Query(NAME_QUERY)
    public Topic findObjectByName(@Param("name") String name);
	
	@Query(TYPE_QUERY)
    public List<Topic> findObjectByType(@Param("type") String type);
}
