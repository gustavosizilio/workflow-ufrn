package org.domain.model.processDefinition;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.domain.model.User;

@Entity
public class UserExecution {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne(cascade=CascadeType.REFRESH)
	private User user;
	@ManyToOne
	private Task task;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startedAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar finishedAt;
	@OneToOne
	private UserExecution nextUserExecution;

	public UserExecution() {
	}
	public UserExecution(User user, boolean start) {
		this.user = user;
		if(start){
			startedAt = Calendar.getInstance();
		}
	}
	public void finish(){
		finishedAt = Calendar.getInstance();
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Double getWastedTime(){
		if(getFinishedAt() != null && getStartedAt() != null){
			return Math.ceil(((float)(getFinishedAt().getTimeInMillis() - getStartedAt().getTimeInMillis())/1000));
		}else{
			return null;
		}
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
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public UserExecution getNextUserExecution() {
		return nextUserExecution;
	}
	public void setNextUserExecution(UserExecution nextUserExecution) {
		this.nextUserExecution = nextUserExecution;
	}
}
