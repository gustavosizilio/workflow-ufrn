package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.domain.model.User;
import org.domain.model.generic.GenericEntity;

@Entity
public class ProcessDefinition extends GenericEntity{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private Workflow workflow;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="processDefinition")
	private List<UserAssignment> userAssignments;
	
	
	@OneToOne(cascade=CascadeType.ALL, mappedBy="processDefinition")
	private StartState startState;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="processDefinition")
	private List<TaskNode> taskNodes;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="processDefinition")
	private List<Join> joins;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="processDefinition")
	private List<Fork> forks;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="processDefinition")
	private List<EndState> endStates;
	
	public ProcessDefinition() {
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
	public Workflow getWorkflow() {
		return workflow;
	}
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
	
	public List<Artefact> getArtefacts() {
		List<Artefact> artefacts = new ArrayList<Artefact>();
		List<TaskNode> taskNodes = getTaskNodes();
		for (TaskNode taskNode : taskNodes) {
			artefacts.addAll(taskNode.getArtefacts());
		}
		return artefacts;
	}
	public List<Artefact> getInArtefacts() {
		List<Artefact> artefacts = new ArrayList<Artefact>();
		List<TaskNode> taskNodes = getTaskNodes();
		for (TaskNode taskNode : taskNodes) {
			artefacts.addAll(taskNode.getInArtefacts());
			for (Task task : taskNode.getTasks()) {
				artefacts.addAll(task.getInArtefacts());
			}
		}
		return artefacts;
	}
	public TaskNode getTaskNode(Transition transition) {
		if(transition != null){
			for (TaskNode taskNode : taskNodes) {
				if(taskNode.getName().equals(transition.getDestination())){
					return taskNode;
				}
			}
		}
		return null;
	}
	
	public boolean canExecute(UserAssignment ua){
		if(!this.workflow.isStarted())
			return false;
		
		if(this.workflow.getTurnQuantity() > 1){
			if(ua.getExecutionOrder() > this.workflow.getCurrentTurn()){
				return false;
			}
		}
		return true;
	}
	
	public Transition getTransitionFor(TaskNode taskNodeFor) {
		for (TaskNode taskNode : taskNodes) {
			for (Transition transition : taskNode.getTransitions()) {
				if(taskNodeFor.getName().equals(transition.getDestination())){
					return transition;
				}
			}
		}
		return null;
	}
	
	public Join getJoin(Transition transition) {
		if(transition != null){
			for (Join join : joins) {
				if(join.getName().equals(transition.getDestination())){
					return join;
				}
			}
		}
		return null;
	}
	public EndState getEndState(Transition transition) {
		if(transition != null){
			for (EndState endState : endStates) {
				if(endState.getName().equals(transition.getDestination())){
					return endState;
				}
			}
		}
		return null;
	}
	
	
	public boolean equals(Object process){
		if(process instanceof ProcessDefinition){
			if(((ProcessDefinition)process).getId().equals(this.getId())){
				return true;
			}
		}
		return false;
	}
	
	public Set<User> getUsers() {
		Set<User> users = new HashSet<User>();
		for (UserAssignment userAssignment : getUserAssignments()) {
			users.add(userAssignment.getUser());
		}
		return users;
	}
	public List<UserAssignment> getUserAssignments() {
		return userAssignments;
	}
	public void setUserAssignments(List<UserAssignment> userAssignments) {
		this.userAssignments = userAssignments;
	}
	public String getPathGrafico(){
		return "../graficos_gerados/"+workflow.getId()+"_"+getId()+".png";
	}
	
}
