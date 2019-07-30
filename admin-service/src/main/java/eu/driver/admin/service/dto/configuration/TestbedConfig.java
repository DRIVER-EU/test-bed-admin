package eu.driver.admin.service.dto.configuration;

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

@Entity
@Table(name="testbedconfig", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="TestbedConfig.findAll", query="SELECT u FROM TestbedConfig u")
public class TestbedConfig {
	
	@Id
	@SequenceGenerator(sequenceName = "admin_service.testbedconfig_seq", name = "TestbedConfigIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TestbedConfigIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="configName", length=255)
	@Size(min = 4, max = 255)
	private String configName;
	
	@Column(name="testbedMode", length=255)
	@Size(min = 4, max = 255)
	private String testbedMode;
	
	@Column(name="active")
	private Boolean isActive;
	
	public TestbedConfig() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getTestbedMode() {
		return testbedMode;
	}

	public void setTestbedMode(String testbedMode) {
		this.testbedMode = testbedMode;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
}
