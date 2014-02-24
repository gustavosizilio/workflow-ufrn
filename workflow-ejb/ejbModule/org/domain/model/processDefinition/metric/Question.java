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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.domain.model.User;

@Entity
public class Question {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String description;
	@ManyToOne
	private Questionnaire questionnaire;
	@Enumerated(EnumType.STRING)
	private QuestionType type;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="question")
	private List<QuestionOption> options;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="question")
	private List<UserAnswer> userAnswers;
	
	public Question() {
		super();
		this.options = new ArrayList<QuestionOption>();
		this.userAnswers = new ArrayList<UserAnswer>();
	}
	
	@Transient
	private List<UserAnswer> answers;
	
	
	public Boolean hasOptions(){
		return (getOptions() != null && getOptions().size() > 0);
	}
	
	public Boolean isCheckBox(){
		if(getType() == QuestionType.CHECKBOX)
			return true;
		
		return false;
	}
	public Boolean isComboBox(){
		if(getType() == QuestionType.COMBOBOX)
			return true;
		
		return false;
	}
	public Boolean isText(){
		if(getType() == QuestionType.TEXT)
			return true;
		
		return false;
	}
	public Boolean isParagraphText(){
		if(getType() == QuestionType.PARAGRAPH)
			return true;
		
		return false;
	}
	
	
	public Boolean needOptions(){
		if(getType() == QuestionType.CHECKBOX || getType() == QuestionType.COMBOBOX)
			return true;
		
		return false;
	}
	
	public QuestionOption getOption(Long id){
		for (QuestionOption op : getOptions()) {
			if(id == op.getId())
				return op;
		}
		
		return null;
	}

	public QuestionType getType() {
		return type;
	}

	public void setType(QuestionType type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	public List<QuestionOption> getOptions() {
		return options;
	}

	public void setOptions(List<QuestionOption> options) {
		this.options = options;
	}
	
	public UserAnswer getUserAnswer(User u, Metric m) {
		UserAnswer an = null;
		for (UserAnswer a : getUserAnswers()) {
			if(a.getUser().getId().equals(u.getId())){
				if(m != null) {
					if(a.getMetric().getId().equals(m.getId())){
						an = a;
					}
				} else {
					an = a;
				}
			}
		}
		if (an == null){
			an = new UserAnswer(u);
			an.setQuestion(this);
			an.setCreatedAt(new GregorianCalendar());
			an.setMetric(m);
			getUserAnswers().add(an);
		}
		return an;
	}
	
	public UserAnswer getUserAnswer(User u) {
		return getUserAnswer(u, null);
	}

	public List<UserAnswer> getUserAnswers() {
		return userAnswers;
	}

	public void setUserAnswers(List<UserAnswer> userAnswers) {
		this.userAnswers = userAnswers;
	}

	public boolean isFinished(User user, Metric metric) {
		UserAnswer an = this.getUserAnswer(user, metric);
		if(an == null || an.getAnswer() == null || an.getAnswer().isEmpty()){
			return false;
		}
		return true;
	}
	public boolean isFinished(User user) {
		UserAnswer an = this.getUserAnswer(user);
		if(an == null || an.getAnswer() == null || an.getAnswer().isEmpty()){
			return false;
		}
		return true;
	}
}
