package eu.driver.admin.service.dto.configuration;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import eu.driver.admin.service.dto.gateway.Gateway;
import eu.driver.admin.service.dto.solution.Solution;
import eu.driver.admin.service.dto.topic.Topic;

@Entity
@Table(name="configuration", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Configuration.findAll", query="SELECT u FROM Configuration u")
public class Configuration {
	
	@Id
	@SequenceGenerator(sequenceName = "admin_service.configuration_seq", name = "ConfigurationIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ConfigurationIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="name", length=255)
	@Size(min = 4, max = 255)
	private String name;
	
	@Column(name="description", length=255)
	@Size(min = 4, max = 255)
	private String discription;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Solution> solutions;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Topic> topics;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Gateway> gateways;
	
	public Configuration() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}

	public List<Solution> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<Solution> solutions) {
		this.solutions = solutions;
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public List<Gateway> getGateways() {
		return gateways;
	}

	public void setGateways(List<Gateway> gateways) {
		this.gateways = gateways;
	}
}
