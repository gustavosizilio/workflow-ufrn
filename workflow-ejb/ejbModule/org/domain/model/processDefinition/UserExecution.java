package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
	private TaskNode taskNode;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startedAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar finishedAt;
	@OneToOne
	private UserExecution nextUserExecution;
	private String comment;
	@OneToMany(mappedBy="userExecution")
	private List<ArtefactFile> artefactFiles;

	public UserExecution() {
		this.artefactFiles = new ArrayList<ArtefactFile>();
	}
	public UserExecution(User user, boolean start) {
		this();
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
	
	public UserExecution getNextUserExecution() {
		return nextUserExecution;
	}
	public void setNextUserExecution(UserExecution nextUserExecution) {
		this.nextUserExecution = nextUserExecution;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public TaskNode getTaskNode() {
		return taskNode;
	}
	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}
	public List<ArtefactFile> getArtefactFiles() {
		return artefactFiles;
	}
	public void setArtefactFiles(List<ArtefactFile> artefactFiles) {
		this.artefactFiles = artefactFiles;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof UserExecution){
			if(((UserExecution)obj).getId().equals(this.getId())){
				return true;
			}
		}
		return false;
	}
}
