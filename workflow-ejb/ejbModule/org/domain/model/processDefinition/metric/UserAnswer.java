
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

import org.domain.model.processDefinition.Field;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;

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
	
	@ManyToOne(cascade=CascadeType.REFRESH)
	private Question question;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private TaskNode taskNode;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private ProcessDefinition processDefinition;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private Workflow workflow;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private Field field;
	
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

	public String getAnswerString() {
		if(getQuestion().hasOptions()){
			if(getAnswer() != null) {
				if(getAnswer().contains(";")){
					String r = "";
					for (String string : getAnswerAsList()) {
						r += getQuestion().getOption(Long.parseLong(string)).getDescription() + ";";
					}
					return r;
				} else {
					return getQuestion().getOption(Long.parseLong(getAnswer())).getDescription();
				}		
			} else {
				return "";
			}
		} else {			
			return answer;
		}
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

	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
}
