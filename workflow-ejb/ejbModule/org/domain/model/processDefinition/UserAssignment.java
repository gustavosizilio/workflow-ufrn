package org.domain.model.processDefinition;

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
import org.domain.model.processDefinition.metric.UserAnswer;

@Entity
public class UserAssignment implements Comparable<UserAssignment>{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne(cascade=CascadeType.REFRESH)
	private User user;
	private Integer executionOrder;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private ProcessDefinition processDefinition;
	private String groupValue;
	private String subjectDiscriminator;
	private String keyFactors;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="userAssignment")
	private List<UserAnswer> userAnswers;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="userAssignment")
	private List<UserExecution> userExecutions;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="userAssignment")
	private List<TaskExecution> taskExecutions;
	
	
	public UserAssignment(Integer executionOrder,
						  ProcessDefinition processDefinition,
						  String subjectDiscriminator,
						  String groupValue,
						  String keyFactors) {
		super();
		this.executionOrder = executionOrder;
		this.processDefinition = processDefinition;
		this.groupValue = groupValue;
		this.subjectDiscriminator = subjectDiscriminator;
		this.setKeyFactors(keyFactors);
	}
	public UserAssignment(String subjectDiscriminator, ProcessDefinition process,  String keyFactors) {
		this();
		this.subjectDiscriminator = subjectDiscriminator;
		this.processDefinition = process;
		this.setKeyFactors(keyFactors);
	}
	
	private UserAssignment() {
		this.executionOrder = 0;
	}
	public UserAssignment(User user, ProcessDefinition process, String keyFactors) {
		this();
		this.setUser(user);
		this.processDefinition = process;
		this.setKeyFactors(keyFactors);
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
	public String getSubjectDiscriminator() {
		return subjectDiscriminator;
	}
	public void setSubjectDiscriminator(String subjectDescription) {
		this.subjectDiscriminator = subjectDescription;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append((this.user != null) ? this.user.toString() : "<empty subject>");
		sb.append((this.executionOrder != null) ? " ["+this.executionOrder +"]": " [<no order associated>]");
		sb.append((this.processDefinition != null) ? " | process -> " + this.processDefinition.getName() : " | process -> <no process associated>");
		sb.append((this.keyFactors != null) ? " ("+this.keyFactors+")" : "");
		
		return sb.toString();
	}
	public String getString() {
		return this.toString();
	}
	public int compareTo(UserAssignment o) {
		if(this.executionOrder > o.getExecutionOrder()){
			return 1;
		}
		if(this.executionOrder < o.getExecutionOrder()){
			return -1;
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj!= null && obj instanceof UserAssignment) {
			return ((UserAssignment)obj).getId().equals(this.getId());
		}
		
		return false;
	}
	
	public String getKeyFactors() {
		return keyFactors;
	}
	public void setKeyFactors(String keyFactors) {
		this.keyFactors = keyFactors;
	}
	public List<UserAnswer> getUserAnswers() {
		return userAnswers;
	}
	public void setUserAnswers(List<UserAnswer> userAnswers) {
		this.userAnswers = userAnswers;
	}
	public List<UserExecution> getUserExecutions() {
		return userExecutions;
	}
	public void setUserExecutions(List<UserExecution> userExecutions) {
		this.userExecutions = userExecutions;
	}
	public List<TaskExecution> getTaskExecutions() {
		return taskExecutions;
	}
	public void setTaskExecutions(List<TaskExecution> taskExecutions) {
		this.taskExecutions = taskExecutions;
	}
}
