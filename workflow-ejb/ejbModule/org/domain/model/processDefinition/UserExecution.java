package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

@Entity
public class UserExecution {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne(cascade=CascadeType.REFRESH)
	private UserAssignment userAssignment;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private TaskNode taskNode;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startedAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar finishedAt;
	@OneToOne(cascade=CascadeType.REFRESH)
	private UserExecution nextUserExecution;
	private String comment;
	@OneToMany(mappedBy="userExecution", cascade=CascadeType.ALL)
	private List<ArtefactFile> artefactFiles;
	@OneToMany(mappedBy="userExecution", cascade=CascadeType.ALL)
	private List<Break> breakes;

	public UserExecution() {
		this.artefactFiles = new ArrayList<ArtefactFile>();
		this.breakes = new ArrayList<Break>();
	}
	
	public UserExecution(UserAssignment userAssignment, boolean start) {
		this();
		this.userAssignment = userAssignment;
		if(start){
			startedAt = Calendar.getInstance();
		}
	}
	
	public List<TaskExecution> getTasksExecutions(){
		List<TaskExecution> taskExecutions = new ArrayList<TaskExecution>();
		for (Task task : this.getTaskNode().getTasks()) {
			TaskExecution te = task.getTaskExecutionByUserAssignment(this.userAssignment);
			if(te!=null){
				taskExecutions.add(te);
			}
		}
		return taskExecutions;
	}
	public TaskExecution getTaskExecution(Task task){
		TaskExecution te = task.getTaskExecutionByUserAssignment(this.userAssignment);
		return te;
	}
	public void finish(){
		finishedAt = Calendar.getInstance();
	}
	
	public boolean isFinished(){
		return this.finishedAt != null;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getWastedTime(){
		if(getFinishedAt() != null && getStartedAt() != null){
			long wastedTime = (long)(getFinishedAt().getTimeInMillis() - getStartedAt().getTimeInMillis());
			wastedTime-=getWastedBreakTime();
			return wastedTime;
		}else{
			return null;
		}
	}
	public String getWastedTimeString(){
		String result = "In progress";
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
	
	public String getWastedBreakTimeString(){
		String result = "In progress";
		Long d = getWastedBreakTime();
		if(d!=null && d > 0){
			result = String.format("%d min, %d sec", 
				    TimeUnit.MILLISECONDS.toMinutes(d),
				    TimeUnit.MILLISECONDS.toSeconds(d) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(d))
				);
		}
		return result;
	}
	
	public Long getWastedBreakTime(){
		Long breakTimes = 0L;
		for (Break b : this.breakes) {
			breakTimes+=b.getWastedTime();
		}
		
		return breakTimes;
	}
	
	public boolean isBreak(){
		return (getOpenedBreak() == null) ? false : true;  
	}
	
	public Break getOpenedBreak(){
		for (Break b : this.breakes) {
			if(b.getFinishedAt() == null){
				return b;
			}
		}
		return null;
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
	public List<Break> getBreakes() {
		return breakes;
	}
	public void setBreakes(List<Break> breakes) {
		this.breakes = breakes;
	}
	public UserAssignment getUserAssignment() {
		return userAssignment;
	}
	public void setUserAssignment(UserAssignment userAssignment) {
		this.userAssignment = userAssignment;
	}
}
