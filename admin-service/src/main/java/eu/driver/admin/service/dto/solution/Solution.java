package eu.driver.admin.service.dto.solution;

import java.util.Date;

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

/**
 * The persistent class for the solution database table.
 * 
 */
@Entity
@Table(name="solution", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Solution.findAll", query="SELECT u FROM Solution u")
public class Solution {
	
	@Id
	@SequenceGenerator(sequenceName = "admin_service.solution_seq", name = "SolutionIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SolutionIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="clientId", length=255)
	@Size(min = 4, max = 255)
	private String clientId = null;
	
	@Column(name="subjectId", length=255)
	@Size(min = 4, max = 255)
	private String subjectId = null;
	
	@Column(name="name", length=255)
	@Size(min = 4, max = 255)
	private String name = null;
	
	@Column(name="isAdmin")
	private Boolean isAdmin = false;
	
	@Column(name="isService")
	private Boolean isService = false;
	
	@Column(name="state")
	private Boolean state = false;
	
	@Column(name="description", columnDefinition="text")
	private String description = null;
	
	@Column(name="lastHeartBeatReceived")
	private Date lastHeartBeatReceived = null;
	
	public Solution() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}
	
	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public Boolean getIsService() {
		return isService;
	}

	public void setIsService(Boolean isService) {
		this.isService = isService;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getLastHeartBeatReceived() {
		return lastHeartBeatReceived;
	}

	public void setLastHeartBeatReceived(Date lastHeartBeatReceived) {
		this.lastHeartBeatReceived = lastHeartBeatReceived;
	}
}
