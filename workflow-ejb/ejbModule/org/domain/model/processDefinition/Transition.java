package org.domain.model.processDefinition;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Transition {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String destination; //Replace for TO attribute... conflict with JPA
	@ManyToOne
	private TaskNode taskNode;
	@ManyToOne
	private Join join;
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
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String toString(){
		return "{name='"+this.name+"', to='"+this.destination + "'}";
	}
	public Join getJoin() {
		return join;
	}
	public void setJoin(Join join) {
		this.join = join;
	}
	public TaskNode getTaskNode() {
		return taskNode;
	}
	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}
}
