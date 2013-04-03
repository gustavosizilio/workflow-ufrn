package org.domain.model.processDefinition;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
	@ManyToOne
	private TaskExecution taskExecution;
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
	
	public Break(TaskExecution taskExecution, boolean start) {
		this();
		this.taskExecution = taskExecution;
		if(start){
			this.startedAt = Calendar.getInstance();
		}
	}

	public Long getWastedTime(){
		if(getFinishedAt() != null && getStartedAt() != null){
			return (Long)(getFinishedAt().getTimeInMillis() - getStartedAt().getTimeInMillis());
		}else{
			return null;
		}
	}
	public String getWastedTimeString(){
		String result = "";
		Long d = getWastedTime();
		if(d!=null){
			result = String.format("%d min, %d sec", 
				    TimeUnit.MILLISECONDS.toMinutes(d),
				    TimeUnit.MILLISECONDS.toSeconds(d) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(d))
				);
		}
		return result;
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

	public TaskExecution getTaskExecution() {
		return taskExecution;
	}

	public void setTaskExecution(TaskExecution taskExecution) {
		this.taskExecution = taskExecution;
	}

}
