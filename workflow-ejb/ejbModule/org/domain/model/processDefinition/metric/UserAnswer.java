package org.domain.model.processDefinition.metric;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.UserAssignment;

@Entity
public class UserAnswer {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne(cascade=CascadeType.REFRESH)
	private UserAssignment userAssignment;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar createdAt;
	private String answer;
	
	@Transient
	private List<String> answerAsList;
	
	@ManyToOne
	private Question question;
	@ManyToOne
	private TaskNode taskNode;
	
	public UserAnswer() {
		// TODO Auto-generated constructor stub
	}
	
	public UserAnswer(UserAssignment userAssignment) {
		this.userAssignment = userAssignment;
	}
	
	/*public Object getValue(){
		if(getQuestion().hasOptions()){
			return getQuestion().getOption(Long.parseLong(getAnswer()));
		}
		
		return answer;
	}*/


	public Calendar getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(Calendar createdAt) {
		this.createdAt = createdAt;
	}


	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public List<String> getAnswerAsList() {
		if(answer!=null)
			return Arrays.asList(answer.split(";"));
		
		return null;
	}

	public void setAnswerAsList(List<String> answerAsList) {
		StringBuilder sb = new StringBuilder();
		for (String string : answerAsList) {
			sb.append(string+";");
		}
		this.answer = sb.toString();
		this.answerAsList = answerAsList;
	}

	public TaskNode getTaskNode() {
		return taskNode;
	}

	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}

	public UserAssignment getUserAssignment() {
		return userAssignment;
	}

	public void setUserAssignment(UserAssignment userAssignment) {
		this.userAssignment = userAssignment;
	}
}
