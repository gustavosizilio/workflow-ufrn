package org.domain.workflow.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.domain.dao.SeamDAO;
import org.domain.dao.UserAssignmentDAO;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.Break;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Field;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.StartState;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.TaskExecution;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.UserExecution;
import org.domain.model.processDefinition.metric.Question;
import org.domain.model.processDefinition.metric.Questionnaire;
import org.domain.model.processDefinition.metric.UserAnswer;
import org.domain.utils.PathBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.richfaces.event.UploadEvent;

@Name("executer")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowExecuter {
	@In("userAssignmentDao") protected UserAssignmentDAO userAssignmentDao;
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	@In(value = "pathBuilder", create = true)  protected PathBuilder pathBuilder;
	
	@In
	private FacesMessages facesMessages;
	
	private static final String WORKFLOW_LIST_EXECUTE_XHTML = "/workflow/listExecute.xhtml";
	private static final String WORKFLOW_EXECUTE_XHTML = "/workflow/execute.xhtml";
	private List<UserAssignment> entities;
	
	private UserAssignment userAssignment;
	private ProcessDefinition currentProcess;
	private TaskNode currentTaskNode;
	private UserExecution currentUserExecution;
	private TaskExecution currentTaskExecution;
	private StartState startState;
	private Join currentJoin;
	private EndState endState;
	private ArtefactFile currentArtefactFile;
	private Artefact currentArtefact;
	private Task currentTask;
	private Questionnaire currentQuestionnaire;
	private List<Questionnaire> currentQuestionnaires;
	
	
	public String init(ProcessDefinition process, UserAssignment ua){
		this.userAssignment = ua;
		if(process.canExecute(ua)){
			this.currentQuestionnaires = new ArrayList<Questionnaire>();
			this.setCurrentProcess(process);
			this.setStartState(process.getStartState());
			this.setCurrentTaskNode(null);
			
			setCurrentJoin(null);
			setEndState(null);
			setCurrentUserExecution(null);
			
			loadQuestionnaires();
			
			return WORKFLOW_EXECUTE_XHTML;
		}else{
			facesMessages.add("Workflow not started yet.");
			return WORKFLOW_LIST_EXECUTE_XHTML;
		}
	}
	
	public String list(){
		findEntities();
		return WORKFLOW_LIST_EXECUTE_XHTML;
	}
	
	public List<Transition> getTransitions(){
		List<Transition> transitions = new ArrayList<Transition>();
		if(currentTaskNode == null){
			if(this.startState != null){
				transitions.addAll(this.startState.getTransitions());
			}
		} else {
			transitions.addAll(currentTaskNode.getTransitions());
		}
		if(currentJoin != null){
			transitions.addAll(currentJoin.getTransitions());			
		}
		return transitions;
	}
	
	public Boolean isStartingState(){
		if(currentTaskNode == null){
			if(this.startState != null){
				return true;
			}
		}
		return false;
	}
	
	public Boolean isEndingState(){
		if(this.endState != null){
			return true;
		}
		return false;
	}
	
	public void start(){
		if(!validateQuestionnaires()) {
			return;
		}
		startUserExecution(true);
		loadTaskExecution();
		loadQuestionnaires();
	}
	
	public void finish(){
		if(!validateQuestionnaires()) {
			return;
		}
		if(!validateFields()) {
			return;
		}
		if(!validateArtefacts()) {
			return;
		}
		if(validateCurrentTaskNode()){
			this.startState = null;
			finishUserExecution();
			loadQuestionnaires();
		}
	}

	public void next(Transition transition){
		if(!validateQuestionnaires()) {
			return;
		}
		currentTaskNode = currentProcess.getTaskNode(transition);
		if(this.currentTaskNode != null){
			this.startState = null;
		}
		currentJoin = currentProcess.getJoin(transition);
		endState = currentProcess.getEndState(transition);
		startUserExecution(false);
		loadQuestionnaires();
	}
	
	private void loadQuestionnaires() {
		this.currentQuestionnaires.clear();
		if(this.startState != null) {
			this.currentQuestionnaires.addAll(this.currentProcess.getWorkflow().getPreQuestionnaires());
			this.currentQuestionnaires.addAll(this.currentProcess.getPreQuestionnaires());
		}
		
		if(this.endState != null) {
			this.currentQuestionnaires.addAll(this.currentProcess.getPostQuestionnaires());
			this.currentQuestionnaires.addAll(this.currentProcess.getWorkflow().getPostQuestionnaires());
		}	
		
		if(this.currentTaskNode != null) {
			this.currentQuestionnaires.addAll(this.currentTaskNode.getPreQuestionnaires());
			if(this.currentUserExecution.getFinishedAt() != null) {
				this.currentQuestionnaires.addAll(this.currentTaskNode.getPostQuestionnaires());
			}
		}
	}
	
	private void loadTaskExecution() {
		setCurrentTask(null);
		setCurrentTaskExecution(null);
		if(currentTaskNode != null) {
			for (Task task : currentTaskNode.getTasks()) {
				if(task.startedByUserAssignment(userAssignment) && !task.finishedByUserAssignment(userAssignment)){
					setCurrentTask(task);
					setCurrentTaskExecution(currentTask.getTaskExecutionByUserAssignment(userAssignment));
					stopBreakCurrentTask();
					stopBreakOthersTasks();
					break;
				}
			}		
		}
	}

	public void startTask(Task task){
		this.currentTask = task;
		startTaskExecution();
		stopBreakCurrentTask();
		breakOthersTasks();
	}
	
	private void stopBreakCurrentTask() {
		TaskExecution taskExecution = this.currentTask.getTaskExecutionByUserAssignment(userAssignment);
		for (Break break1 : taskExecution.getBreakes()) {
			if(break1.getFinishedAt() == null){
				break1.setFinishedAt(new GregorianCalendar());
				this.seamDao.merge(break1);
				this.seamDao.flush();
			}
		}
		
	}
	
	private void stopBreakOthersTasks() {
		for (Task task : this.currentTaskNode.getTasks()) {
			if(!task.equals(this.currentTask)){
				TaskExecution taskExecution = task.getTaskExecutionByUserAssignment(userAssignment);
				if(taskExecution != null){
					for (Break break1 : taskExecution.getBreakes()) {
						if(break1.getFinishedAt() == null){
							break1.setFinishedAt(new GregorianCalendar());
							this.seamDao.merge(break1);
							this.seamDao.flush();
						}
					}
				}
			}
		}
	}

	private void breakOthersTasks() {
		for (Task task : currentTaskNode.getTasks()) {
			TaskExecution taskExecution = task.getTaskExecutionByUserAssignment(userAssignment);
			if(taskExecution != null && !taskExecution.equals(currentTaskExecution) && !taskExecution.isFinished()){
				Break newBreak = new Break(task.getTaskExecutionByUserAssignment(userAssignment), true);
				newBreak.setReason("Starting another task");
				this.seamDao.persist(newBreak);
				
				taskExecution.getBreakes().add(newBreak);
				this.seamDao.merge(newBreak);
				this.seamDao.flush();
			}
		}
	}

	private void startTaskExecution() {
		if(this.currentTask != null){
			if(!this.currentTask.startedByUserAssignment(userAssignment)){
					TaskExecution taskExecution = new TaskExecution(userAssignment, true);
					taskExecution.setTask(currentTask);
					seamDao.persist(taskExecution);
					currentTask.getTaskExecutions().add(taskExecution);
					seamDao.merge(currentTask);
					seamDao.flush();
					setCurrentTaskExecution(taskExecution);
			} else {
				setCurrentTaskExecution(currentTask.getTaskExecutionByUserAssignment(userAssignment));
			}
		}
	}
	
	public void finishTaskExecution() {
		if(currentTaskExecution != null && currentTask != null){
			if(!currentTask.finishedByUserAssignment(userAssignment)){
				if(validateCurrentTask()){
					currentTaskExecution.finish();			
					seamDao.merge(currentTaskExecution);
					seamDao.flush();
				}
			}
		}
	}
	
	private boolean validateCurrentTask() {
		if(currentTask != null){
			List<Artefact> artefacts = currentTask.getOutArtefacts();

			for (Artefact artefact : artefacts) {
				if(artefact.get(currentUserExecution) == null){
					facesMessages.add(Severity.ERROR, "You should send all artefacts!");
					return false;
				}
			}
			
		}
		return true;
	}
	
	public boolean isCurrentTask(Task task){
		if(currentTask == null || task == null)
			return false;
		return task.equals(currentTask);
	}
	
	public void startBreak(){
		if(this.currentUserExecution.getFinishedAt() == null) {
			Break newBreak = new Break(this.currentUserExecution, true);
			if(currentTaskExecution != null && !currentTaskExecution.isFinished()){
				newBreak.setTaskExecution(currentTaskExecution);
			}
			this.seamDao.persist(newBreak);
			this.currentUserExecution.getBreakes().add(newBreak);
			if(currentTaskExecution != null && !currentTaskExecution.isFinished()){
				currentTaskExecution.getBreakes().add(newBreak);
				this.seamDao.merge(currentTaskExecution);
			}
			this.seamDao.merge(this.currentUserExecution);
			this.seamDao.merge(this.currentUserExecution);
			this.seamDao.flush();
		}
	}
	
	public void stopBreak(){
		if(this.currentUserExecution.isBreak()) {
			Break openedBreak = this.currentUserExecution.getOpenedBreak();
			openedBreak.setFinishedAt(Calendar.getInstance());
			this.seamDao.merge(openedBreak);
			this.seamDao.flush();
		}
	}

	private boolean validateQuestionnaires() {
		for (Questionnaire q : this.currentQuestionnaires) {
			if(!q.isFinished(userAssignment, this.currentTaskNode)){
				facesMessages.add(Severity.ERROR, "You should complete all questionnaires!");
				return false;
			}
		}
		return true;
	}
	private boolean validateFields() {
		for (Field f : this.currentTaskNode.getFields()) {
			if(!f.isFinished(userAssignment)){
				facesMessages.add(Severity.ERROR, "You should complete all fields!");
				return false;
			}
		}
		return true;
	}
	private boolean validateArtefacts() {
		for (Artefact artefact : this.currentTaskNode.getOutArtefacts()) {
			if(artefact.get(currentUserExecution) == null){
				facesMessages.add(Severity.ERROR, "You should send all artefacts!");
				return false;
			}
		}
		return true;
	}

	private boolean validateCurrentTaskNode() {
		if(currentTaskNode != null){
			for (Task task : currentTaskNode.getTasks()) {
				if(!task.finishedByUserAssignment(userAssignment)){
					facesMessages.add(Severity.ERROR, "You should finish all tasks!");
					return false;
				}
			}	
		}
		return true;
	}
	
	public void setCurrentArtefact(Artefact artefact){
		this.currentArtefact = artefact;
	}
	
	public void setCurrentQuestionnaire(Questionnaire questionnaire){
		this.currentQuestionnaire = questionnaire;
	}
	
	public void saveQuestionnaire(){
		for (Question q : this.currentQuestionnaire.getQuestions()) {
			UserAnswer uan = q.getUserAssignmentAnswer(userAssignment, this.currentTaskNode);
			if(uan.getId() == null ||  uan.getId() == 0){
				seamDao.persist(uan);
			} else {
				seamDao.merge(uan);
			}
			seamDao.merge(q);
		}
		
		seamDao.flush();
	}
	
	public Questionnaire getCurrentQuestionnaire(){
		return this.currentQuestionnaire;
	}
	
	public void removeArtefact(Artefact artefact) throws Exception {
		List<ArtefactFile> artefactsFiles = new ArrayList<ArtefactFile>(artefact.getArtefactFiles());
		for (ArtefactFile artefactFile : artefactsFiles) {
			if(artefactFile.getUserExecution().equals(currentUserExecution)){
				seamDao.remove(artefactFile);
				artefact.getArtefactFiles().remove(artefactFile);
				new File(artefactFile.getFile()).delete();
			}
		}
		seamDao.merge(artefact);
		seamDao.flush();
	}
	
	public void uploadArtefact(UploadEvent event) throws Exception {
		ArtefactFile artefactfile = new ArtefactFile();
		seamDao.persist(artefactfile);
		
		//String path = ReadPropertiesFile.getProperty("components", "artefactPath");
		//path = path + this.currentArtefact.getId() + "/" + artefactfile.getId() + "/";
		String path = pathBuilder.getArtefactsPath(this.currentTaskNode.getProcessDefinition().getWorkflow(), this.currentArtefact);
		File upload = new File(path);
		upload.mkdirs();
		
		path = path + event.getUploadItem().getFileName();
	    if(event.getUploadItem().getFile().renameTo(new File(path))){
	    	artefactfile.setFile(path);		
	    	artefactfile.setArtefact(this.currentArtefact);
	    	artefactfile.setUserExecution(currentUserExecution);
	    	seamDao.merge(artefactfile);
	    	this.currentArtefact.getArtefactFiles().add(artefactfile);
	    	seamDao.merge(this.currentArtefact);
	    	seamDao.flush();
	    }
	}
	
	
	private void startUserExecution(boolean start) {
		if(currentTaskNode != null){
			if(!currentTaskNode.startedByUserAssignment(userAssignment)){
					UserExecution userExecution = new UserExecution(userAssignment, start);
					userExecution.setTaskNode(currentTaskNode);
					seamDao.persist(userExecution);
					currentTaskNode.getUserExecutions().add(userExecution);
					seamDao.merge(currentTaskNode);
					if(currentUserExecution != null){
						currentUserExecution.setNextUserExecution(userExecution);
						seamDao.persist(currentUserExecution);
					}
					seamDao.flush();
					currentUserExecution = userExecution;
			} else {
				UserExecution userExecution = currentTaskNode.getUserExecutionByUserAssignment(userAssignment);
				if(userExecution.getStartedAt() == null && start){
					userExecution.setStartedAt(Calendar.getInstance());
				}
				this.currentUserExecution = userExecution;
				seamDao.merge(currentUserExecution);
				seamDao.flush();
				setCurrentUserExecution(userExecution);
			}
		}
	}

	private void finishUserExecution() {
		if(currentUserExecution != null && currentTaskNode != null){
			if(!currentTaskNode.finishedByUserAssignment(userAssignment)){
				currentUserExecution.finish();			
				seamDao.merge(currentUserExecution);
				seamDao.flush();
			}
		}
	}

	public void findEntities()
	{
		List<UserAssignment> userAssignments = this.userAssignmentDao.findAllOpened(user);
		Collections.sort(userAssignments);
		setEntities(userAssignments);
	}

	public List<UserAssignment> getEntities() {
		return entities;
	}

	public void setEntities(List<UserAssignment> entities) {
		this.entities = entities;
	}

	public ProcessDefinition getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(ProcessDefinition currentProcess) {
		this.currentProcess = currentProcess;
	}

	public Join getCurrentJoin() {
		return currentJoin;
	}

	public void setCurrentJoin(Join currentJoin) {
		this.currentJoin = currentJoin;
	}

	public EndState getEndState() {
		return endState;
	}

	public void setEndState(EndState endState) {
		this.endState = endState;
	}

	public UserExecution getCurrentUserExecution() {
		return currentUserExecution;
	}

	public void setCurrentUserExecution(UserExecution currentUserExecution) {
		this.currentUserExecution = currentUserExecution;
	}

	public TaskNode getCurrentTaskNode() {
		return currentTaskNode;
	}

	public void setCurrentTaskNode(TaskNode currentTaskNode) {
		this.currentTaskNode = currentTaskNode;
	}

	public ArtefactFile getCurrentArtefactFile() {
		return currentArtefactFile;
	}

	public void setCurrentArtefactFile(ArtefactFile currentArtefactFile) {
		this.currentArtefactFile = currentArtefactFile;
	}

	public StartState getStartState() {
		return startState;
	}

	public void setStartState(StartState startState) {
		this.startState = startState;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public TaskExecution getCurrentTaskExecution() {
		return currentTaskExecution;
	}

	public void setCurrentTaskExecution(TaskExecution currentTaskExecution) {
		this.currentTaskExecution = currentTaskExecution;
	}

	public UserAssignment getUserAssignment() {
		return userAssignment;
	}

	public void setUserAssignment(UserAssignment userAssignment) {
		this.userAssignment = userAssignment;
	}

	public List<Questionnaire> getCurrentQuestionnaires() {
		return currentQuestionnaires;
	}

	public void setCurrentQuestionnaires(List<Questionnaire> currentQuestionnaires) {
		this.currentQuestionnaires = currentQuestionnaires;
	}
}
