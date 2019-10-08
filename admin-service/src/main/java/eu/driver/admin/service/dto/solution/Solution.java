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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.springframework.data.rest.webmvc.support.BackendId;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
	
	@Column(name="orgName", length=255)
	@Size(min = 4, max = 255)
	private String orgName = null;
	
	@Column(name="userName", length=255)
	@Size(min = 4, max = 255)
	private String userName = null;
	
	@Column(name="userPwd", length=255)
	@Size(min = 4, max = 255)
	private String userPwd = null;
	
	@Column(name="certPwd", length=255)
	@Size(min = 4, max = 255)
	private String certPwd = null;
	
	@Column(name="email", length=255)
	@Size(max = 255)
	private String email = null;
	
	@Column(name="description", columnDefinition="text")
	private String description = null;
	
	@Column(name="lastHeartBeatReceived")
	private Date lastHeartBeatReceived = null;
	
	@ManyToOne
	private Organisation organisation = null;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
	  name = "admin_service.applied_solutions", 
	  joinColumns = @JoinColumn(name = "solution_id"), 
	  inverseJoinColumns = @JoinColumn(name = "configuration_id"))
	@JsonBackReference
	private List<Configuration> applConfigurations;
	
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
	
	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}

	public String getCertPwd() {
		return certPwd;
	}

	public void setCertPwd(String certPwd) {
		this.certPwd = certPwd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public List<Configuration> getApplConfigurations() {
		return applConfigurations;
	}

	public void setApplConfigurations(List<Configuration> applConfigurations) {
		this.applConfigurations = applConfigurations;
	}
	
	public void addApplConfigurations(Configuration applConfiguration) {
		if (this.applConfigurations == null) {
			this.applConfigurations = new ArrayList<Configuration>();
		}
		this.applConfigurations.add(applConfiguration);
	}
	
	
}
