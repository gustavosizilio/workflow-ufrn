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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.domain.model.User;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.Workflow;

@Entity
public class Questionnaire {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private Workflow workflow;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="questionnaire")
	private List<Metric> metrics;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="questionnaire")
	private List<Question> questions;
	@Enumerated(EnumType.STRING)
	private QuestionnaireType questionnaireType;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private ProcessDefinition process;
	@Transient
	private String processName;
	
	
	public Questionnaire(String name) {
		this.metrics = new ArrayList<Metric>();
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
	public List<Metric> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
	
	public QuestionnaireType getQuestionnaireType() {
		return questionnaireType;
	}
	public void setQuestionnaireType(QuestionnaireType questionnaireType) {
		this.questionnaireType = questionnaireType;
	}
	public ProcessDefinition getProcess() {
		return process;
	}
	public void setProcess(ProcessDefinition process) {
		this.process = process;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public Workflow getWorkflow() {
		return workflow;
	}
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
	public boolean isFinished(User user, Metric metric) {
		for (Question q : this.getQuestions()) {
			if(!q.isFinished(user, metric))
				return false;
		}
		return true;
	}
	public boolean isFinished(User user) {
		for (Question q : this.getQuestions()) {
			if(!q.isFinished(user))
				return false;
		}
		return true;
	}
}
