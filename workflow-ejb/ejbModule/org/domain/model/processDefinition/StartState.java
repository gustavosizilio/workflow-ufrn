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

@Entity
public class StartState {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@OneToOne(cascade=CascadeType.ALL)
	private Task task;
	@OneToMany(cascade=CascadeType.ALL)
	private List<Transition> transitions;
	@OneToMany(cascade=CascadeType.ALL)
	private List<Event> events;
		
	public StartState() {
		transitions = new ArrayList<Transition>();
		events = new ArrayList<Event>();
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
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
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
	
	/*public String toString(){
		String transitionsString = "";
		for (Transition transition : transitions) {
			transitionsString += transition;
		}
		return "{transitions=["+transitionsString+"]}";
	}*/
	
}
