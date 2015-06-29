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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.UserAssignment;

@Entity
public class Question {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Lob
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
	
	public Boolean isRadioButtons(){
		if(getType() == QuestionType.RADIOBUTTONS)
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
			if(id.equals(op.getId()))
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
	
	public UserAnswer getUserAssignmentAnswer(UserAssignment userAssignment, TaskNode task) {
		if(userAssignment != null) {
			boolean isWorkflowQuest = false;
			boolean isProcessQuest = false;
			boolean isTaskNodeQuest = false;
			
			if(this.getQuestionnaire().getWorkflows().size() > 0) {
				isWorkflowQuest = true;
			}
			if(this.getQuestionnaire().getProcessDefinitions().size() > 0){
				isProcessQuest = true;
			}
			if(this.questionnaire.getTaskNodes().size() > 0) {
				isTaskNodeQuest = true;
			}
			
			
			UserAnswer an = null;
			for (UserAnswer a : getUserAnswers()) {
				if(isWorkflowQuest) {
					if(this.getQuestionnaire().getWorkflows().contains(userAssignment.getProcessDefinition().getWorkflow()) 
						&&	a.getUserAssignment().getUser().getId().equals(userAssignment.getUser().getId())){
						an = a;
						break;
					}
				} else if (isProcessQuest) {
					if(this.getQuestionnaire().getProcessDefinitions().contains(userAssignment.getProcessDefinition()) 
							&&	a.getUserAssignment().equals(userAssignment)){
							an = a;
							break;
					}
				} else  if(isTaskNodeQuest){
					if(this.getQuestionnaire().getTaskNodes().contains(task) 
						&& a.getUserAssignment().equals(userAssignment)){
						if(a.getTaskNode() != null && a.getTaskNode().getId().equals(task.getId())){
							an = a;
							break;
						}		
					}
				}
			}
			if (an == null){
				an = new UserAnswer(userAssignment);
				an.setQuestion(this);
				an.setCreatedAt(new GregorianCalendar());
				if(isWorkflowQuest) {
					an.setWorkflow(userAssignment.getProcessDefinition().getWorkflow());
				} else if (isProcessQuest) {
					an.setProcessDefinition(userAssignment.getProcessDefinition());
				} else if (isTaskNodeQuest){
					an.setTaskNode(task);
				}
				getUserAnswers().add(an);
			}
			return an;
		} else {
			return null;
		}
	}
	
	public UserAnswer getUserAssignmentAnswer(UserAssignment userAssignment) {
		return getUserAssignmentAnswer(userAssignment, null);
	}

	public List<UserAnswer> getUserAnswers() {
		return userAnswers;
	}

	public void setUserAnswers(List<UserAnswer> userAnswers) {
		this.userAnswers = userAnswers;
	}

	public boolean isFinished(UserAssignment userAssignment, TaskNode task) {
		UserAnswer an = this.getUserAssignmentAnswer(userAssignment, task);
		if(an == null || an.getAnswer() == null || an.getAnswer().isEmpty()){
			return false;
		}
		return true;
	}
	/*public boolean isFinished(UserAssignment userAssignment) {
		UserAnswer an = this.getUserAssignmentAnswer(userAssignment);
		if(an == null || an.getAnswer() == null || an.getAnswer().isEmpty()){
			return false;
		}
		return true;
	}*/
}
