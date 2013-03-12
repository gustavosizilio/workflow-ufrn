package org.domain.model.processDefinition;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.domain.model.User;

@Entity
public class UserAssignment{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne
	private User user;
	private Integer executionOrder;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private ProcessDefinition processDefinition;
	private String groupValue;
	private String subjectDescription;
	
	
	public UserAssignment() {
	}
	public UserAssignment(String subjectDescription, ProcessDefinition process) {
		this();
		this.subjectDescription = subjectDescription;
		this.processDefinition = process;
		this.executionOrder = 0;
	}
	public UserAssignment(User user, ProcessDefinition process) {
		this();
		this.setUser(user);
		this.processDefinition = process;
		this.executionOrder = 0;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}
	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Integer getExecutionOrder() {
		return executionOrder;
	}
	public void setExecutionOrder(Integer executionOrder) {
		this.executionOrder = executionOrder;
	}
	public String getGroupValue() {
		return groupValue;
	}
	public void setGroupValue(String groupValue) {
		this.groupValue = groupValue;
	}
	public String getSubjectDescription() {
		return subjectDescription;
	}
	public void setSubjectDescription(String subjectDescription) {
		this.subjectDescription = subjectDescription;
	}
}
