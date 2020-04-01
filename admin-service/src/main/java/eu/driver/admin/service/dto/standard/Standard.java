package eu.driver.admin.service.dto.standard;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(name="standard", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Standard.findAll", query="SELECT u FROM Standard u")
public class Standard {

	@Id
	@SequenceGenerator(sequenceName = "admin_service.standard_seq", name = "StandardIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "StandardIdSequence")
	@Column(unique=true, nullable=false)
	@JsonIgnore
	private Long id;
	
	@Column(name="name", length=255)
	@Size(min = 1, max = 255)
	private String name = null;
	
	@Column(name="namespace", length=1024)
	@Size(min = 1, max = 1024)
	private String namespace = null;
	
	@ElementCollection
	@CollectionTable(name="version", schema="admin_service", joinColumns=@JoinColumn(name="id"))
	@Column(name="versions")
	private List<String> versions = new ArrayList<String>();
	
	@Column(name="fileName", length=1024)
	@Size(min = 1, max = 1024)
	private String fileName = null;
	
	public Standard() {
		
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
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
