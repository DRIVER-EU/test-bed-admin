package eu.driver.admin.service.dto.topic;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.driver.admin.service.dto.configuration.Configuration;

/**
 * The persistent class for the topic database table.
 * 
 */
@Entity
@Table(name="topic", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Topic.findAll", query="SELECT u FROM Topic u")
public class Topic {
	@Id
	@SequenceGenerator(sequenceName = "admin_service.gateway_seq", name = "TopicIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TopicIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="clientId", length=255)
	@Size(min = 4, max = 255)
	private String clientId = null;
	
	@Column(name="type", length=255)
	@Size(min = 4, max = 255)
	private String type = null;
	
	@Column(name="name", length=255)
	@Size(min = 4, max = 255)
	private String name = null;
	
	@Column(name="state")
	private Boolean state = false;
	
	@Column(name="msgType", length=25)
	@Size(min = 1, max = 25)
	private String msgType = null;
	
	@Column(name="msgTypeVersion", length=25)
	@Size(min = 1, max = 25)
	private String msgTypeVersion = null;
	
	@Column(name="description", columnDefinition="text")
	private String description = null;
	
	@ElementCollection
	@CollectionTable(name="publishSolutions", schema="admin_service", joinColumns=@JoinColumn(name="id"))
	@Column(name="publishSolutionIDs")
	private List<String> publishSolutionIDs = new ArrayList<String>();
	
	@ElementCollection
	@CollectionTable(name="subscribedSolutions", schema="admin_service", joinColumns=@JoinColumn(name="id"))
	@Column(name="subscribedSolutionIDs")
	private List<String> subscribedSolutionIDs = new ArrayList<String>();
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
	  name = "admin_service.applied_topics", 
	  joinColumns = @JoinColumn(name = "topic_id"), 
	  inverseJoinColumns = @JoinColumn(name = "configuration_id"))
	@JsonBackReference
	private List<Configuration> applConfigurations;
	
	public Topic() {
		
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

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgTypeVersion() {
		return msgTypeVersion;
	}

	public void setMsgTypeVersion(String msgTypeVersion) {
		this.msgTypeVersion = msgTypeVersion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getPublishSolutionIDs() {
		return publishSolutionIDs;
	}

	public void setPublishSolutionIDs(List<String> publishSolutionIDs) {
		this.publishSolutionIDs = publishSolutionIDs;
	}

	public List<String> getSubscribedSolutionIDs() {
		return subscribedSolutionIDs;
	}

	public void setSubscribedSolutionIDs(List<String> subscribedSolutionIDs) {
		this.subscribedSolutionIDs = subscribedSolutionIDs;
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
