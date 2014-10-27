package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.domain.model.User;
import org.domain.model.generic.GenericEntity;
import org.domain.model.processDefinition.metric.Questionnaire;
import org.domain.model.processDefinition.metric.QuestionnaireType;

@Entity
public class Workflow extends GenericEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private User user;
	
	@Enumerated(EnumType.STRING)
	private DesignType designType;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob
	private byte[] definition;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="workflow")
	private List<ProcessDefinition> processDefinitions;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="workflow")
	private List<Questionnaire> questionnaires;
	
	private Date startedAt;
	
	private Integer turnQuantity;
	private Integer currentTurn;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="workflow")
	private List<Observation> observations;
	
	public Workflow() {
		this.questionnaires = new ArrayList<Questionnaire>();
		this.setProcessDefinitions(new ArrayList<ProcessDefinition>());
	}
	public Workflow(User user) {
		this();
		this.turnQuantity = 0;
		this.user = user;
	}

	@Override
	public void validate() {
		for (ProcessDefinition process : processDefinitions) {
			if(!process.isValid()){
				addErrors(process.getErrors());
			}
		}
	}
	
	public List<Questionnaire> getPreQuestionnaires(){
		List<Questionnaire> preQuestionnaires = new ArrayList<Questionnaire>();
		for (Questionnaire q : questionnaires) {
			if(q.getQuestionnaireType().equals(QuestionnaireType.PRE_EXPERIMENT))
				preQuestionnaires.add(q);
		}
		return preQuestionnaires;
	}
	
	public List<Questionnaire> getPostQuestionnaires(){
		List<Questionnaire> postQuestionnaires = new ArrayList<Questionnaire>();
		for (Questionnaire q : questionnaires) {
			if(q.getQuestionnaireType().equals(QuestionnaireType.POST_EXPERIMENT))
				postQuestionnaires.add(q);
		}
		return postQuestionnaires;
	}
	
	public List<User> getAllUsers(){
		List<User> users = new ArrayList<User>();
		for (ProcessDefinition processDefinition : processDefinitions) {
			users.addAll(processDefinition.getUsers());
		}
		return users;
	}
	public List<UserAssignment> getAllUserAssignments(){
		List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
		for (ProcessDefinition processDefinition : processDefinitions) {
			userAssignments.addAll(processDefinition.getUserAssignments());
		}
		return userAssignments;
	}
	
	/*public ArrayList<Artefact> getAllArtefacts() {
		ArrayList<Artefact> artefacts = new ArrayList<Artefact>();
		for (ProcessDefinition process: getProcessDefinitions()) {
			artefacts.addAll(process.getArtefacts());
		}
		return artefacts;
	}*/
	public ArrayList<Artefact> getInArtefacts() {
		ArrayList<Artefact> artefacts = new ArrayList<Artefact>();
		for (ProcessDefinition process: getProcessDefinitions()) {
			artefacts.addAll(process.getInArtefacts());
		}
		return artefacts;
	}
	
	public boolean isManualDesign(){
		return this.getDesignType() != null && this.getDesignType().equals(DesignType.MANUAL);
	}
	public boolean isRCBDDesign(){
		return this.getDesignType() != null && this.getDesignType().equals(DesignType.RCBD);
	}
	public boolean isCRDesign(){
		return this.getDesignType() != null && this.getDesignType().equals(DesignType.CRD);
	}
	public boolean isLSDesign(){
		return this.getDesignType() != null && this.getDesignType().equals(DesignType.LS);
	}
	public Set<String> getGroups(){
		Set<String> blocks = new HashSet<String>();
		if(isRCBDDesign() || isLSDesign()){
			for (UserAssignment ua : this.getAllUserAssignments()) {
				blocks.add(ua.getGroupValue());
			}
		}
		return blocks;
	}
	public List<UserAssignment> getUserAssignments(String group){
		List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
		for (UserAssignment userAssignment : getAllUserAssignments()) {
			if(userAssignment.getGroupValue().equals(group)){
				userAssignments.add(userAssignment);
			}
		}
		Collections.sort(userAssignments);
		return userAssignments;
	}
	
	public int getQuantityOfSubjectsNeeds(String group){
		int quantityOfSubjectsNeeds = 0;
		List<String> foundSubjects = new ArrayList<String>();
		for (UserAssignment userAssignment : getAllUserAssignments()) {
			if(userAssignment.getGroupValue().equals(group) && !foundSubjects.contains(userAssignment.getSubjectDescription())){
				quantityOfSubjectsNeeds++;
				foundSubjects.add(userAssignment.getSubjectDescription());
			}
		}
		return quantityOfSubjectsNeeds;
	}
	
	public boolean addUserToGroup(String group, User u){
		for (UserAssignment ua : getUserAssignments(group)) {
			if(ua.getUser()==null){
				ua.setUser(u);
				for (UserAssignment ua2 : getUserAssignments(group)) {
					if(ua2.getUser() == null && ua2.getSubjectDescription().equals(ua.getSubjectDescription())){
						ua2.setUser(u);
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean isDesignComplete(){
		for (UserAssignment uAssignment : getAllUserAssignments()) {
			if(uAssignment.getUser()==null){
				return false;
			}
		}
		return true;
	}
	
	public boolean canStart(){
		if(!isDesignComplete())
			return false;
					
		if(getAllUserAssignments().size() == 0)
			return false;
		
		return true;
	}
	
	public Date getStartedAt() {
		return startedAt;
	}
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}
	public boolean isStarted(){
		return this.startedAt != null;
	}

	public boolean isEmptyDesign(){
		return this.getDesignType() == null;
	}
	
	@Override
	public void validateDeletable() {
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	public String getTitle() {
		return "";
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescription() {
		return "";
	}
	public List<ProcessDefinition> getProcessDefinitions() {
		return processDefinitions;
	}
	public void setProcessDefinitions(List<ProcessDefinition> processDefinitions) {
		this.processDefinitions = processDefinitions;
	}
	public List<Observation> getObservations() {
		return observations;
	}
	public void setObservations(List<Observation> observations) {
		this.observations = observations;
	}
	public DesignType getDesignType() {
		return designType;
	}
	public void setDesignType(DesignType designType) {
		this.designType = designType;
	}
	public Integer getTurnQuantity() {
		return turnQuantity;
	}
	public void setTurnQuantity(Integer turnQuantity) {
		this.turnQuantity = turnQuantity;
	}
	public Integer getCurrentTurn() {
		return currentTurn;
	}
	public void setCurrentTurn(Integer currentTurn) {
		this.currentTurn = currentTurn;
	}
	public void nextTurn() {
		if(this.currentTurn == null){
			this.currentTurn = 0;
		}else{
			this.currentTurn++;	
		}
	}
	public boolean isLastTurn(){
		if(this.turnQuantity == null || this.turnQuantity == 0)
			return true;
		
		if(this.getCurrentTurn() == null){
			return false;
		}
		if((this.turnQuantity) > this.getCurrentTurn()){
			return false;
		}else{
			return true;
		}
	}
	public List<Questionnaire> getQuestionnaires() {
		return questionnaires;
	}
	public void setQuestionnaires(List<Questionnaire> questionnaires) {
		this.questionnaires = questionnaires;
	}
	public byte[] getDefinition() {
		return definition;
	}
	public void setDefinition(byte[] definition) {
		this.definition = definition;
	}
}
