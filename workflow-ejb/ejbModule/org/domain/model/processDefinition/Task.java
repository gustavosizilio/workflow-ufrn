package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.domain.model.User;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	@OneToOne(cascade=CascadeType.REFRESH)
	private StartState startState;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private TaskNode taskNode;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<Artefact> artefacts;
	
	private String swimlane;
	/*
	@Enumerated(EnumType.STRING)
	private PriorityType priority;
	*/
	@OneToMany(mappedBy="task",cascade=CascadeType.ALL)
	private List<UserExecution> userExecutions;
	
	public Task() {
		this.userExecutions = new ArrayList<UserExecution>();
		this.artefacts = new ArrayList<Artefact>();
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
	
	/*
	public PriorityType getPriority() {
		return priority;
	}
	public void setPriority(PriorityType priority) {
		this.priority = priority;
	}
	*/
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String toString(){
		String artefactsString = "";
		for (Artefact artefact : this.artefacts) {
			artefactsString += artefact.toString() + ",";
		}
		return "{name='"+this.name+"' description='"+this.description+"', swimlane='"+this.getSwimlane() +
				"', artefacts=["+ artefactsString +"]}";
	}
	public StartState getStartState() {
		return startState;
	}
	public void setStartState(StartState startState) {
		this.startState = startState;
	}
	public TaskNode getTaskNode() {
		return taskNode;
	}
	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}
	public String getSwimlane() {
		return swimlane;
	}
	public void setSwimlane(String swimlane) {
		this.swimlane = swimlane;
	}
	public List<UserExecution> getUserExecutions() {
		return userExecutions;
	}
	public void setUserExecutions(List<UserExecution> userExecutions) {
		this.userExecutions = userExecutions;
	}
	public boolean startedByUser(User user){
		if(getUserExecutionByUser(user) != null){
			return true;
		} else {
			return false;
		}
	}
	public boolean finishedByUser(User user){
		UserExecution userExecution = getUserExecutionByUser(user);
		if(userExecution != null && userExecution.getFinishedAt() != null){
			return true;
		} else {
			return false;
		}
	}
	
	public UserExecution getUserExecutionByUser(User user){
		for (UserExecution userExecution : userExecutions) {
			if(userExecution.getUser().equals(user)){
				return userExecution;
			}
		}
		return null;
	}
	public List<Artefact> getArtefacts() {
		return artefacts;
	}
	public void setArtefacts(List<Artefact> artefacts) {
		this.artefacts = artefacts;
	}
}
