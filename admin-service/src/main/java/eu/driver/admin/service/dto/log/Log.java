package eu.driver.admin.service.dto.log;

import java.io.Serializable;
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


/**
 * The persistent class for the user database table.
 * 
 */
@Entity
@Table(name="log", schema = "admin_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQuery(name="Log.findAll", query="SELECT u FROM Log u")
public class Log implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(sequenceName = "admin_service.log_seq", name = "LogIdSequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LogIdSequence")
	@Column(unique=true, nullable=false)
	private Long id;
	
	@Column(name="clientId", length=25)
	private String clientId = null;
	
	@Column(name="level", length=25)
	private String level = null;
	
	@Column(name="sendDate")
	private Date sendDate = null;
	
	@Column(name="message", length=2048)
	private String message = null;
	
	public Log() {
		
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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}