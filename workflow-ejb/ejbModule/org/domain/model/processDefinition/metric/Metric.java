package org.domain.model.processDefinition.metric;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
	@ManyToMany
	private List<TaskNode>  taskNodes;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="metric")
	private List<UserAnswer> userAnswers;	
	
	public Metric() {
		this.userAnswers = new ArrayList<UserAnswer>();
		this.setTaskNodes(new ArrayList<TaskNode>());
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

	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	public boolean finishedByUser(User user, TaskNode task) {
		if(metricType.equals(MetricType.QUEST)){
			return validateQuestionnaire(user, task);
		}
		return true;
	}

	private boolean validateQuestionnaire(User user, TaskNode task) {
		return questionnaire.isFinished(user, this, task);
	}

	public List<TaskNode> getTaskNodes() {
		return taskNodes;
	}

	public void setTaskNodes(List<TaskNode> taskNodes) {
		this.taskNodes = taskNodes;
	}

	public List<UserAnswer> getUserAnswers() {
		return userAnswers;
	}
	
	public UserAnswer getUserAnswer(User u, TaskNode task) {
		UserAnswer an = null;
		for (UserAnswer ua : userAnswers) {
			if(ua.getUser().getId().equals(u.getId())){
				if(ua.getTaskNode() != null && ua.getTaskNode().getId().equals(task.getId())){
					return ua;
				}
			}
		}
		
		if (an == null){
			an = new UserAnswer(u);
			an.setCreatedAt(new GregorianCalendar());
			an.setMetric(this);
			an.setTaskNode(task);
			getUserAnswers().add(an);
		}
		
		return an;
	}

	public void setUserAnswers(List<UserAnswer> userAnswers) {
		this.userAnswers = userAnswers;
	}

}
