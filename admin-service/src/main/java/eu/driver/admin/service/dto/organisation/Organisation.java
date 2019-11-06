package eu.driver.admin.service.dto.organisation;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.driver.admin.service.dto.solution.Solution;


@Entity
@Table(name="organisation", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Organisation.findAll", query="SELECT u FROM Organisation u")
public class Organisation {

	@Id
	@SequenceGenerator(sequenceName = "admin_service.organisation_seq", name = "OrganisationIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OrganisationIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
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
	@Size(min = 4, max = 255)
	private String email = null;
	
	@Column(name="phone", length=255)
	@Size(min = 4, max = 255)
	private String phone = null;
	
	@Column(name="city", length=255)
	@Size(min = 4, max = 255)
	private String city = null;	
	
	@Column(name="postcode")
	private Integer postcode = null;
		
	@Column(name="street", length=255)
	@Size(min = 4, max = 255)
	private String street = null;
		
	@Column(name="nr")
	private Integer nr = null;
	
	@Column(name="description", columnDefinition="text")
	private String description = null;
	
	@OneToMany(fetch=FetchType.LAZY,
	        orphanRemoval = true,
	        mappedBy="organisation")
	@JsonIgnore
	private List<Solution> solutions;

	public Organisation() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getPostcode() {
		return postcode;
	}

	public void setPostcode(Integer postcode) {
		this.postcode = postcode;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public Integer getNr() {
		return nr;
	}

	public void setNr(Integer nr) {
		this.nr = nr;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Solution> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<Solution> solutions) {
		this.solutions = solutions;
	}
	
	public void addSolution(Solution solution) {
		if (this.solutions == null) {
			this.solutions = new ArrayList<Solution>();
		}
		this.solutions.add(solution);
	}
}
