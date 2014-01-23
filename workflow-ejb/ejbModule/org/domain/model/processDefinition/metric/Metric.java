package org.domain.model.processDefinition.metric;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.domain.model.User;
import org.domain.model.processDefinition.TaskNode;

@Entity
public class Metric {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String refName;
	@Enumerated(EnumType.STRING)
	private MetricType metricType;
	@ManyToOne
	private Questionnaire questionnaire;
	@ManyToOne
	private TaskNode  taskNode;
	
	public Metric() {
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

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public MetricType getMetricType() {
		return metricType;
	}

	public void setMetricType(MetricType metricType) {
		this.metricType = metricType;
	}

	public TaskNode getTaskNode() {
		return taskNode;
	}

	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}

	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	public boolean finishedByUser(User user) {
		if(metricType.equals(MetricType.QUEST)){
			return validateQuestionnaire(user);
		}
		return true;
	}

	private boolean validateQuestionnaire(User user) {
		for (Question q : questionnaire.getQuestions()) {
			UserAnswer an = q.getUserAnswer(user, this);
			if(an == null || an.getAnswer() == null || an.getAnswer().isEmpty()){
				return false;
			}
		}
		return true;
	}

}
