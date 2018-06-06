package eu.driver.admin.service.dto.gateway;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

/**
 * The persistent class for the gateway database table.
 * 
 */
@Entity
@Table(name="gateway", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Gateway.findAll", query="SELECT u FROM Gateway u")
public class Gateway {
	
	@Id
	@SequenceGenerator(sequenceName = "admin_service.gateway_seq", name = "GatewayIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GatewayIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="clientId", length=255)
	@Size(min = 4, max = 255)
	private String clientId = null;
	
	@Column(name="name", length=255)
	@Size(min = 4, max = 255)
	private String name = null;
	
	@Column(name="state")
	private Boolean state = false;
	
	@Column(name="description", columnDefinition="text")
	private String description = null;
	
	
	@ElementCollection
	@CollectionTable(name="managingTypes", schema="admin_service", joinColumns=@JoinColumn(name="id"))
	@Column(name="managingType")
	private List<String> managingType = new ArrayList<String>();
	
	public Gateway() {
		
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getManagingType() {
		return managingType;
	}

	public void setManagingType(List<String> managingType) {
		this.managingType = managingType;
	}
}
