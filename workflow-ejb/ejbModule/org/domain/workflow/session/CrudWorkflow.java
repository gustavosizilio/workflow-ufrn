package org.domain.workflow.session;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.domain.dao.UserDAO;
import org.domain.dao.WorkflowDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.Swimlane;
import org.domain.model.processDefinition.Workflow;
import org.domain.utils.ReadPropertiesFile;
import org.domain.workflow.session.generic.CrudAction;
import org.domain.xml.Manager;
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
	@In("workflowDAO") WorkflowDAO workflowDAO;
	private User userProperty;
	private Swimlane swimlaneProperty;
	private Artefact currentArtefact;
	
	public CrudWorkflow() {
		super(Workflow.class);
	}
	
	@Override
	public void findEntities()
	{
		setEntities(this.workflowDAO.findAllByUser(user));
	}
	
	@Override
	protected void createImpl(){
		this.entity.setUser(user);
	}
	
	//TODO parse jpdl
	public void deployWorkflow(UploadEvent event) throws Exception {
	    UploadItem item = event.getUploadItem();
	    Manager jpdl = new Manager(item.getFile().getAbsolutePath());
	    
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
	
	public void setCurrentArtefact(Artefact artefact){
		this.currentArtefact = artefact;
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
	    	seamDao.merge(artefactfile);
	    	this.currentArtefact.getArtefactFiles().add(artefactfile);
	    	seamDao.merge(this.currentArtefact);
	    	seamDao.flush();
	    }
	}
	
	public void removeArtefact(Artefact artefact) throws Exception {
		List<ArtefactFile> artefactsFiles = artefact.getArtefactFiles();
		for (ArtefactFile artefactFile : artefactsFiles) {
			seamDao.remove(artefactFile);
		}
		artefact.getArtefactFiles().clear();
		seamDao.merge(artefact);
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

	@Override
	protected Workflow getExampleForFind() {
		return new Workflow();
	}
}
