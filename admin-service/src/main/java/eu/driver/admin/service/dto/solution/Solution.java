package eu.driver.admin.service.dto.solution;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.driver.admin.service.dto.configuration.Configuration;
import eu.driver.admin.service.dto.organisation.Organisation;

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
	@JsonIgnore
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
	
	@OneToOne(fetch=FetchType.EAGER)
	private Origin origin = null;
	
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
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "organisation_id")
	private Organisation organisation = null;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
	  name = "admin_service.applied_solutions", 
	  joinColumns = @JoinColumn(name = "solution_id"), 
	  inverseJoinColumns = @JoinColumn(name = "configuration_id"))
	@JsonIgnore
	private List<Configuration> applSolConfigurations;
	
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
	
	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
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
	
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public List<Configuration> getApplSolConfigurations() {
		return applSolConfigurations;
	}

	public void setApplSolConfigurations(List<Configuration> applSolConfigurations) {
		this.applSolConfigurations = applSolConfigurations;
	}
	
	public void addApplSolConfigurations(Configuration applConfiguration) {
		if (this.applSolConfigurations == null) {
			this.applSolConfigurations = new ArrayList<Configuration>();
		}
		this.applSolConfigurations.add(applConfiguration);
	}
	
	
}
