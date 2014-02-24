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

import org.domain.model.User;

@Entity
public class UserAnswer {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne(cascade=CascadeType.REFRESH)
	private User user;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar createdAt;
	private String answer;
	
	@Transient
	private List<String> answerAsList;
	
	@ManyToOne
	private Question question;
	@ManyToOne
	private Metric metric;
	
	public UserAnswer() {
		// TODO Auto-generated constructor stub
	}
	
	public UserAnswer(User u) {
		this.user = u;
	}
	
	/*public Object getValue(){
		if(getQuestion().hasOptions()){
			return getQuestion().getOption(Long.parseLong(getAnswer()));
		}
		
		return answer;
	}*/


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


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

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
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
}
