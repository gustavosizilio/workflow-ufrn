package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.domain.model.generic.GenericEntity;

@Entity
public class ProcessDefinition extends GenericEntity{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;

	@OneToMany(cascade=CascadeType.ALL)
	private List<Swimlane> swimlanes;
	
	@OneToOne(cascade=CascadeType.ALL)
	private StartState startState;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<TaskNode> taskNodes;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<Join> joins;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<Fork> forks;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<EndState> endStates;
	
	public ProcessDefinition() {
		swimlanes = new ArrayList<Swimlane>();
		taskNodes = new ArrayList<TaskNode>();
		joins = new ArrayList<Join>();
		forks = new ArrayList<Fork>();
		endStates = new ArrayList<EndState>();
	}
	public ProcessDefinition(String name){
		this();
		this.name = name;
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
	@Override
	public void validate() {
		if(name == null || name.trim().length() == 0){
			addError("Nome do Processo é obrigatório");
		}
	}
	@Override
	public void validateDeletable() {
		
	}
	public List<Swimlane> getSwimlanes() {
		return swimlanes;
	}
	public void setSwimlanes(List<Swimlane> swimlanes) {
		this.swimlanes = swimlanes;
	}
	public StartState getStartState() {
		return startState;
	}
	public void setStartState(StartState startState) {
		this.startState = startState;
	}
	public List<TaskNode> getTaskNodes() {
		return taskNodes;
	}
	public void setTaskNodes(List<TaskNode> taskNodes) {
		this.taskNodes = taskNodes;
	}
	public List<Join> getJoins() {
		return joins;
	}
	public void setJoins(List<Join> joins) {
		this.joins = joins;
	}
	public List<Fork> getForks() {
		return forks;
	}
	public void setForks(List<Fork> forks) {
		this.forks = forks;
	}
	public List<EndState> getEndStates() {
		return endStates;
	}
	public void setEndStates(List<EndState> endStates) {
		this.endStates = endStates;
	}
	
}
