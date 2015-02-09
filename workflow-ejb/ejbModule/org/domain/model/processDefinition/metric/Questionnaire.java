package org.domain.model.processDefinition.metric;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;

@Entity
public class Questionnaire {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="questionnaire")
	private List<Question> questions;
	@Enumerated(EnumType.STRING)
	private QuestionnaireType questionnaireType;
	
	@ManyToMany(cascade=CascadeType.REFRESH, mappedBy="questionnaires")
	private List<Workflow> workflows;
	@ManyToMany(cascade=CascadeType.REFRESH, mappedBy="questionnaires")
	private List<ProcessDefinition> processDefinitions;
	@ManyToMany(cascade=CascadeType.REFRESH, mappedBy="questionnaires")
	private List<TaskNode> taskNodes;
	@Transient
	private String processName;
	
	
	public Questionnaire(String name) {
		this.questions = new ArrayList<Question>();
		this.name = name;
	}
	public Questionnaire() {
		
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
	public List<Question> getQuestions() {
		return questions;
	}
	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}
	public QuestionnaireType getQuestionnaireType() {
		return questionnaireType;
	}
	public void setQuestionnaireType(QuestionnaireType questionnaireType) {
		this.questionnaireType = questionnaireType;
	}
	
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	public boolean isFinished(UserAssignment userAssignment, TaskNode task) {
		for (Question q : this.getQuestions()) {
			if(!q.isFinished(userAssignment, task))
				return false;
		}
		return true;
	}
	public boolean isFinished(UserAssignment userAssignment) {
		for (Question q : this.getQuestions()) {
			if(!q.isFinished(userAssignment))
				return false;
		}
		return true;
	}
	public List<Workflow> getWorkflows() {
		return workflows;
	}
	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}
	public List<TaskNode> getTaskNodes() {
		return taskNodes;
	}
	public void setTaskNodes(List<TaskNode> taskNodes) {
		this.taskNodes = taskNodes;
	}
	public List<ProcessDefinition> getProcessDefinitions() {
		return processDefinitions;
	}
	public void setProcessDefinitions(List<ProcessDefinition> processDefinitions) {
		this.processDefinitions = processDefinitions;
	}
}
