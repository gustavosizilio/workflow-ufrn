package org.domain.workflow.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.domain.dao.SeamDAO;
import org.domain.dao.UserAssignmentDAO;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.Break;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.StartState;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.UserExecution;
import org.domain.utils.ReadPropertiesFile;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
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
	@In
	private FacesMessages facesMessages;
	
	private static final String WORKFLOW_LIST_EXECUTE_XHTML = "/workflow/listExecute.xhtml";
	private static final String WORKFLOW_EXECUTE_XHTML = "/workflow/execute.xhtml";
	private List<UserAssignment> entities;
	
	private ProcessDefinition currentProcess;
	private TaskNode currentTaskNode;
	private UserExecution currentUserExecution;
	private StartState startState;
	private Join currentJoin;
	private EndState endState;
	private ArtefactFile currentArtefactFile;
	private Artefact currentArtefact;
	
	public String init(ProcessDefinition process){
		if(process.isStarted()){
			this.setCurrentProcess(process);
			this.setStartState(process.getStartState());
			this.setCurrentTaskNode(null);
			setCurrentJoin(null);
			setEndState(null);
			setCurrentUserExecution(null);
			
			return WORKFLOW_EXECUTE_XHTML;
		}else{
			facesMessages.add("Workflow ainda não iniciado.");
			return WORKFLOW_LIST_EXECUTE_XHTML;
		}
	}
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
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
	
	public void next(Transition transition){
		if(validateCurrentTaskNode()){
			this.startState = null;
			finishUserExecution();
			
			currentTaskNode = currentProcess.getTaskNode(transition);
			currentJoin = currentProcess.getJoin(transition);
			endState = currentProcess.getEndState(transition);
			
			startUserExecution();
		}
	}
	
	public void startBreak(){
		if(this.currentUserExecution.getFinishedAt() == null) {
			Break newBreak = new Break(this.currentUserExecution, true);
			this.seamDao.persist(newBreak);
			this.currentUserExecution.getBreakes().add(newBreak);
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

	private boolean validateCurrentTaskNode() {
		if(currentTaskNode != null){
			List<Artefact> artefacts = new ArrayList<Artefact>();
			artefacts.addAll(currentTaskNode.getOutArtefacts());
			
			for (Task task : currentTaskNode.getTasks()) {
				artefacts.addAll(task.getOutArtefacts());
			}

			for (Artefact artefact : artefacts) {
				if(artefact.get(currentUserExecution) == null){
					facesMessages.add(Severity.ERROR, "É preciso enviar todos os artefatos!");
					return false;
				}
			}
			
		}
		return true;
	}
	
	public void setCurrentArtefact(Artefact artefact){
		this.currentArtefact = artefact;
	}
	
	public void removeArtefact(Artefact artefact) throws Exception {
		List<ArtefactFile> artefactsFiles = new ArrayList<ArtefactFile>(artefact.getArtefactFiles());
		for (ArtefactFile artefactFile : artefactsFiles) {
			if(artefactFile.getUserExecution().equals(currentUserExecution)){
				seamDao.remove(artefactFile);
				artefact.getArtefactFiles().remove(artefactFile);
			}
		}
		seamDao.merge(artefact);
		seamDao.flush();
	}
	
	public void uploadArtefact(UploadEvent event) throws Exception {
		ArtefactFile artefactfile = new ArtefactFile();
		seamDao.persist(artefactfile);
		
		String path = ReadPropertiesFile.getProperty("components", "artefactPath");
		path = path + this.currentArtefact.getId() + "/" + artefactfile.getId() + "/";
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
	
	
	private void startUserExecution() {
		if(currentTaskNode != null){
			if(!currentTaskNode.startedByUser(user)){
					UserExecution userExecution = new UserExecution(user, true);
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
				setCurrentUserExecution(currentTaskNode.getUserExecutionByUser(user));
			}
		}
	}

	private void finishUserExecution() {
		if(currentUserExecution != null && currentTaskNode != null){
			if(!currentTaskNode.finishedByUser(user)){
				currentUserExecution.finish();			
				seamDao.merge(currentUserExecution);
				seamDao.flush();
			}
		}
	}

	public void findEntities()
	{
		setEntities(this.userAssignmentDao.findAllOpened(user));
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
}
