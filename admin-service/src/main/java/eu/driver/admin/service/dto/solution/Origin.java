package eu.driver.admin.service.dto.solution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the solution database table.
 * 
 */
@Entity
@Table(name="origin", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Origin.findAll", query="SELECT u FROM Origin u")
public class Origin {
	
	@Id
	@SequenceGenerator(sequenceName = "admin_service.origin_seq", name = "OriginIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OriginIdSequence")
	@Column(unique=true, nullable=false)
	@JsonIgnore
	private Long id;
	
	@Column(name="localIp", length=255)
	@Size(min = 4, max = 255)
	private String localIp = null;
	
	@Column(name="hosteName", length=255)
	@Size(min = 4, max = 255)
	private String hosteName = null;
	
	@Column(name="remoteIp", length=255)
	@Size(min = 4, max = 255)
	private String remoteIp = null;
	
	public Origin() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public String getHosteName() {
		return hosteName;
	}

	public void setHosteName(String hosteName) {
		this.hosteName = hosteName;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

}
