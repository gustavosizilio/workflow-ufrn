package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.domain.model.User;
import org.domain.model.processDefinition.dataType.ArtefactType;
import org.domain.model.processDefinition.metric.Metric;
import org.domain.model.processDefinition.metric.MetricType;

@Entity
public class TaskNode {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Lob
	private String description;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="taskNode")
	private List<Transition> transitions;
	@ManyToOne
	private ProcessDefinition processDefinition;
	@OneToOne(cascade=CascadeType.REFRESH)
	private StartState startState;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="taskNode")
	private List<Artefact> artefacts;
	@ManyToMany(cascade={CascadeType.MERGE,CascadeType.REMOVE})
	private List<Metric> metrics;
	@OneToMany(mappedBy="taskNode",cascade=CascadeType.ALL)
	private List<UserExecution> userExecutions;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="taskNode")
	private List<Task> tasks;
		
	public TaskNode() {
		this.userExecutions = new ArrayList<UserExecution>();
		this.artefacts = new ArrayList<Artefact>();
		this.tasks = new ArrayList<Task>();
		this.transitions = new ArrayList<Transition>();
		this.metrics = new ArrayList<Metric>();
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
	public List<Transition> getTransitions() {
		return transitions;
	}
	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	public StartState getStartState() {
		return startState;
	}

	public void setStartState(StartState startState) {
		this.startState = startState;
	}

	public List<Artefact> getOutArtefacts() {
		List<Artefact> outArtefacts = new ArrayList<Artefact>();
		for (Artefact artefact : artefacts) {
			if(artefact.getArtefactType().equals(ArtefactType.OUT)){
				outArtefacts.add(artefact);
			}
		}
		return outArtefacts;
	}
	public List<Artefact> getInArtefacts() {
		List<Artefact> inArtefacts = new ArrayList<Artefact>();
		for (Artefact artefact : artefacts) {
			if(artefact.getArtefactType().equals(ArtefactType.IN)){
				inArtefacts.add(artefact);
			}
		}
		return inArtefacts;
	}
	public List<Artefact> getArtefacts() {
		return artefacts;
	}
	public void setArtefacts(List<Artefact> artefacts) {
		this.artefacts = artefacts;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}
	
	public List<Metric> getCollectedDataMetrics() {
		List<Metric> metrics = new ArrayList<Metric>();
		for (Metric m : getMetrics()) {
			if(m.getMetricType().equals(MetricType.COLLECTED_DATA)){
				metrics.add(m);
			}
		}
		return metrics;
	}
	
	public List<Metric> getQuestionnaireMetrics() {
		List<Metric> metrics = new ArrayList<Metric>();
		for (Metric m : getMetrics()) {
			if(m.getMetricType().equals(MetricType.QUEST)){
				metrics.add(m);
			}
		}
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
