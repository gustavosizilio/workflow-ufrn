package org.domain.model.processDefinition;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.domain.model.processDefinition.dataType.PriorityType;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	/*private boolean blocking;
	private boolean signalling;
	private String description;
	private String duedate;*/
	private String swimlane;
	@Enumerated(EnumType.STRING)
	private PriorityType priority;
	@OneToOne(cascade=CascadeType.ALL)
	private Controller controller;
	
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
	/*public boolean isBlocking() {
		return blocking;
	}
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
	public boolean isSignalling() {
		return signalling;
	}
	public void setSignalling(boolean signalling) {
		this.signalling = signalling;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDuedate() {
		return duedate;
	}
	public void setDuedate(String duedate) {
		this.duedate = duedate;
	}
	public String getSwimlane() {
		return swimlane;
	}
	public void setSwimlane(String swimlane) {
		this.swimlane = swimlane;
	}*/
	public PriorityType getPriority() {
		return priority;
	}
	public void setPriority(PriorityType priority) {
		this.priority = priority;
	}
	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String toString(){
		/*return "{name="+this.name+", description="+this.description+", swimlane="+this.swimlane+", duedate="+this.duedate+"" +
				", blocking="+this.blocking+", signalling="+this.signalling+", priority="+priority+"" +
				", controller="+controller+"}";*/
		return "{name='"+this.name+"' description='"+this.description+"', swimlane='"+this.swimlane +
				"', priority='"+priority+"'" +
				", controller="+controller+"}";
	}
}
