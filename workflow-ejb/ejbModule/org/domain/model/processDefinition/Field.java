package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.domain.model.processDefinition.metric.UserAnswer;

@Entity
public class Field {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String displayName;
	@ManyToOne
	private TaskNode taskNode;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="field")
	private List<UserAnswer> userAnswers;
	
	public Field() {
		this.userAnswers = new ArrayList<UserAnswer>();
	}

	public UserAnswer getUserAssignmentAnswer(UserAssignment userAssignment) {
		UserAnswer an = null;
		for (UserAnswer a : getUserAnswers()) {
			if(a.getUserAssignment().equals(userAssignment)){
				an = a;
			}
		}
		
		if (an == null){
			an = new UserAnswer(userAssignment);
			an.setCreatedAt(new GregorianCalendar());
			an.setField(this);
			getUserAnswers().add(an);
		}
		return an;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public TaskNode getTaskNode() {
		return taskNode;
	}

	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}

	public List<UserAnswer> getUserAnswers() {
		return userAnswers;
	}

	public void setUserAnswers(List<UserAnswer> userAnswers) {
		this.userAnswers = userAnswers;
	}

	public boolean isFinished(UserAssignment userAssignment) {
		UserAnswer an = this.getUserAssignmentAnswer(userAssignment);
		if(an == null || an.getAnswer() == null || an.getAnswer().isEmpty()){
			return false;
		}
		return true;
	}
	
	

}
