package org.domain.model.processDefinition;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Break {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private UserExecution userExecution;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startedAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar finishedAt;
	private String reason;
	
	public Break() {
		reason = "";
	}
	
	public Break(UserExecution userExecution, boolean start) {
		this();
		this.userExecution = userExecution;
		if(start){
			this.startedAt = Calendar.getInstance();
		}
	}
	
	public Double getWastedTime(){
		if(getFinishedAt() != null && getStartedAt() != null){
			return Math.ceil(((float)(getFinishedAt().getTimeInMillis() - getStartedAt().getTimeInMillis())/1000));
		}else{
			return null;
		}
	}
	
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public UserExecution getUserExecution() {
		return userExecution;
	}


	public void setUserExecution(UserExecution userExecution) {
		this.userExecution = userExecution;
	}


	public Calendar getStartedAt() {
		return startedAt;
	}


	public void setStartedAt(Calendar startedAt) {
		this.startedAt = startedAt;
	}


	public Calendar getFinishedAt() {
		return finishedAt;
	}


	public void setFinishedAt(Calendar finishedAt) {
		this.finishedAt = finishedAt;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
