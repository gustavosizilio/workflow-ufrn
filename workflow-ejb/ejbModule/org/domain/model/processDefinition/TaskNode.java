package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskNode {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private boolean createTasks;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="taskNode")
	private List<Task> tasks;
	@OneToMany(cascade=CascadeType.ALL)
	private List<Transition> transitions;
	@OneToMany(cascade=CascadeType.ALL)
	private List<Event> events;
		
	public TaskNode() {
		transitions = new ArrayList<Transition>();
		events = new ArrayList<Event>();
		tasks = new ArrayList<Task>();
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
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public boolean isCreateTasks() {
		return createTasks;
	}

	public void setCreateTasks(boolean createTasks) {
		this.createTasks = createTasks;
	}
}
