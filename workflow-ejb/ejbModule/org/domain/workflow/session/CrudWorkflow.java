package org.domain.workflow.session;

import java.util.Calendar;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.domain.dao.UserDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.Workflow;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.Swimlane;
import org.domain.workflow.session.generic.CrudAction;
import org.domain.xml.JPDLManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("crudWorkflow")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class CrudWorkflow extends CrudAction<Workflow> {
	
	@In("userDao") UserDAO userDAO;
	private User userProperty;
	private Swimlane swimlaneProperty;
	
	public CrudWorkflow() {
		super(Workflow.class);
	}
	
	@Override
	protected Workflow getExampleForFind() {
		return new Workflow(user);
	}
	
	@Override
	protected void createImpl(){
		this.entity.setUser(user);
	}
	
	//TODO parse jpdl
	public void deployWorkflow(UploadEvent event) throws Exception {
	    UploadItem item = event.getUploadItem();
	    JPDLManager jpdl = new JPDLManager(item.getFile().getAbsolutePath());
	    
	    List<ProcessDefinition> processDefinitions;
		try {
			processDefinitions = jpdl.executeTransformations();
			for (ProcessDefinition process : processDefinitions) {
				process.setWorkflow(this.entity);
				seamDao.persist(process);
			}
			this.entity.getProcessDefinitions().addAll(processDefinitions);
		    seamDao.merge(entity);
		    seamDao.flush();
		} catch (ValidationException e) {
			addErrors(e.getErrors());
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String undeployWorkflow() {
		for (ProcessDefinition process : entity.getProcessDefinitions()) {
			seamDao.remove(process);
		}
		entity.getProcessDefinitions().clear();
		seamDao.flush();
		seamDao.refresh(entity);
		addInfo("Undeploy efetuado com sucesso");
		return getPage();
	}
	
	public void addUserParticipant(ActionEvent evt) throws ValidationException{
		if(swimlaneProperty != null && userProperty != null){
			seamDao.refresh(swimlaneProperty);
			if(!swimlaneProperty.getUsers().contains(userProperty)){
				swimlaneProperty.getUsers().add(userProperty);
				seamDao.merge(swimlaneProperty);
				swimlaneProperty.getProcessDefinition().updatelUsers();
				seamDao.merge(swimlaneProperty.getProcessDefinition());
				seamDao.flush();
			}
			userProperty = null;
			swimlaneProperty = null;
		}
	}
	
	public void removeUserParticipant(Swimlane swimlane, User user) throws ValidationException{
		seamDao.refresh(swimlane);
		seamDao.refresh(user);
		swimlane.getUsers().remove(user);
		seamDao.merge(swimlane);
		swimlane.getProcessDefinition().updatelUsers();
		seamDao.merge(swimlane.getProcessDefinition());
		seamDao.flush();			
	}
	
	public void start(ProcessDefinition processDefinition) throws ValidationException{
		seamDao.refresh(processDefinition);
		processDefinition.setStartedAt(Calendar.getInstance().getTime());
		seamDao.merge(processDefinition);
		seamDao.flush();
	}
	
	public List<User> getUsers(){
		return userDAO.findAll(User.class);
	}
	
	public User getUserProperty() {
		return userProperty;
	}

	public void setUserProperty(User userProperty) {
		this.userProperty = userProperty;
	}

	public Swimlane getSwimlaneProperty() {
		return swimlaneProperty;
	}

	public void setSwimlaneProperty(Swimlane swimlaneProperty) {
		this.swimlaneProperty = swimlaneProperty;
	}
}
