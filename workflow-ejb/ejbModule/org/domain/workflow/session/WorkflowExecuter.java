package org.domain.workflow.session;

import java.util.ArrayList;
import java.util.List;

import org.domain.dao.ProcessDefinitionDAO;
import org.domain.dao.SeamDAO;
import org.domain.model.User;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.UserExecution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

@Name("executer")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowExecuter {
	@In("processDao") protected ProcessDefinitionDAO processDao;
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	@In
	private FacesMessages facesMessages;
	
	private static final String WORKFLOW_LIST_EXECUTE_XHTML = "/workflow/listExecute.xhtml";
	private static final String WORKFLOW_EXECUTE_XHTML = "/workflow/execute.xhtml";
	private List<ProcessDefinition> entities;
	
	private ProcessDefinition currentProcess;
	private Task currentTask;
	private UserExecution currentUserExecution;
	private Join currentJoin;
	private EndState endState;
	
	public String init(ProcessDefinition process){
		this.setCurrentProcess(process);
		this.setCurrentTask(process.getStartState().getTask());
		setCurrentJoin(null);
		setEndState(null);
		setCurrentUserExecution(null);
		
		return WORKFLOW_EXECUTE_XHTML;
	}
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
	public String list(){
		findEntities();
		return WORKFLOW_LIST_EXECUTE_XHTML;
	}
	
	public List<Transition> getTransitions(){
		List<Transition> transitions = new ArrayList<Transition>();
		if(currentTask != null){
			if(currentTask.getStartState() != null){
				transitions.addAll(currentTask.getStartState().getTransitions());
			}
			if(currentTask.getTaskNode() != null){
				transitions.addAll(currentTask.getTaskNode().getTransitions());
			}
		}
		if(currentJoin != null){
			transitions.addAll(currentJoin.getTransitions());			
		}
		return transitions;
	}
	
	public void next(Transition transition){
		if(validateCurrentTask()){
			finishUserExecution();
			
			currentTask = currentProcess.getTask(transition.getDestination());
			currentJoin = currentProcess.getJoin(transition.getDestination());
			endState = currentProcess.getEndState(transition.getDestination());
			
			startUserExecution();
		}
	}

	private boolean validateCurrentTask() {
		if(currentTask != null){
			if(currentTask.getArtefacts().size() > 0){
				facesMessages.add(Severity.ERROR, "É preciso enviar todos os artefatos!");
				return false;
			}
		}
		return true;
	}

	private void startUserExecution() {
		if(currentTask != null){
			if(!currentTask.startedByUser(user)){
					UserExecution userExecution = new UserExecution(user, true);
					userExecution.setTask(currentTask);
					seamDao.persist(userExecution);
					currentTask.getUserExecutions().add(userExecution);
					seamDao.merge(currentTask);
					if(currentUserExecution != null){
						currentUserExecution.setNextUserExecution(userExecution);
						seamDao.persist(currentUserExecution);
					}
					seamDao.flush();
					currentUserExecution = userExecution;
			} else {
				setCurrentUserExecution(currentTask.getUserExecutionByUser(user));
			}
		}
	}

	private void finishUserExecution() {
		if(currentUserExecution != null && currentTask != null){
			if(!currentTask.finishedByUser(user)){
				currentUserExecution.finish();			
				seamDao.merge(currentUserExecution);
				seamDao.flush();
			}
		}
	}

	public void findEntities()
	{
		setEntities(this.processDao.findAllOpened(user));
	}

	public List<ProcessDefinition> getEntities() {
		return entities;
	}

	public void setEntities(List<ProcessDefinition> entities) {
		this.entities = entities;
	}

	public ProcessDefinition getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(ProcessDefinition currentProcess) {
		this.currentProcess = currentProcess;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
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
}
