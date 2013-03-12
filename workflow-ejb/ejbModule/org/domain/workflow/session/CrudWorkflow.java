package org.domain.workflow.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.domain.dao.UserDAO;
import org.domain.dao.WorkflowDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.DesignType;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;
import org.domain.utils.ReadPropertiesFile;
import org.domain.workflow.session.generic.CrudAction;
import org.domain.xml.DesignConfigurationManager;
import org.domain.xml.WorkflowManager;
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
	private String groupProperty;
	private Map<String,List<User>> usersSelectedToShuffle;
	private ProcessDefinition processDefinitionProperty;
	private Artefact currentArtefact;
	
	public CrudWorkflow() {
		super(Workflow.class);
		this.setUsersSelectedToShuffle(new Hashtable<String,List<User>>());
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
	    WorkflowManager jpdl = new WorkflowManager(item.getFile().getAbsolutePath());
	    
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
	
	public void addUserToShuffle(ActionEvent evt) throws ValidationException{
		if(this.userProperty != null && this.groupProperty != null && !isUserPresentToShuffle(this.userProperty)){
			if(!this.getUsersSelectedToShuffle().containsKey(this.groupProperty)){
				this.getUsersSelectedToShuffle().put(this.groupProperty, new ArrayList<User>());
			}
			this.getUsersSelectedToShuffle().get(this.groupProperty).add(this.userProperty);
		}
		this.userProperty = null;
	}
	private boolean isUserPresentToShuffle(User user) {
		for (List<User> users : this.getUsersSelectedToShuffle().values()) {
			for (User u : users) {
				if(user.equals(u)){
					return true;
				}
			}
		}
		return false;
	}

	public void removeUserToSuffle(User u, String group){
		this.usersSelectedToShuffle.get(group).remove(u);
	}
	public List<String> getGroupValues(){
		List<String> groups = new ArrayList<String>();
		for (String string : this.getUsersSelectedToShuffle().keySet()) {
			groups.add(string);
		}
		return groups;
	}
	public void suffleUsersRCDB() throws ValidationException{
		boolean hasError = false;
		for (String groupValue : getGroupValues()) {
			if(this.getUsersSelectedToShuffle().get(groupValue).size() < this.entity.getUserAssignments(groupValue).size()){
				getFacesMessages().add("Quantidade de usuários insuficiente no grupo "+groupValue);
				hasError = true;
			}
		}
		if(!hasError){
			for (String groupValue : getGroupValues()) {
				List<User> users = this.getUsersSelectedToShuffle().get(groupValue);
				Collections.shuffle(users);
				for (User user : users) {
					this.entity.addUserToGroup(groupValue, user);
				}
			}
		}
		
		seamDao.merge(this.entity);
		seamDao.flush();
	}
	
	public void addUserManual(ActionEvent evt) throws ValidationException{
		seamDao.refresh(entity);
		if(userProperty != null && getProcessDefinitionProperty() != null && !getProcessDefinitionProperty().getUsers().contains(userProperty)
				&& entity.isManualDesign() ){
			UserAssignment userAssignment = new UserAssignment(userProperty, getProcessDefinitionProperty());
			seamDao.persist(userAssignment);
			seamDao.refresh(getProcessDefinitionProperty());

			getProcessDefinitionProperty().getUserAssignments().add(userAssignment);
			seamDao.merge(getProcessDefinitionProperty());
			
			seamDao.flush();
			userProperty = null;
			processDefinitionProperty = null;
		}
	}
	
	public void removeUserManual(UserAssignment userAssignment) throws ValidationException{
		if(entity.isManualDesign()  ){
			seamDao.refresh(userAssignment);
			seamDao.remove(userAssignment);
			
			processDefinitionProperty = userAssignment.getProcessDefinition();
			userProperty = userAssignment.getUser();
			
			processDefinitionProperty.getUserAssignments().remove(userAssignment);
			seamDao.merge(processDefinitionProperty);
			
			seamDao.flush();
		}
	}
	
	public void start(ProcessDefinition processDefinition) throws ValidationException{
		seamDao.refresh(processDefinition);
		processDefinition.setStartedAt(Calendar.getInstance().getTime());
		seamDao.merge(processDefinition);
		seamDao.flush();
	}
	
	public void updateDesignTypeToManual(){
		try {
			seamDao.refresh(entity);
			this.entity.setDesignType(DesignType.MANUAL);
			seamDao.merge(entity);
			seamDao.flush();
		} catch (ValidationException e) {
			getFacesMessages().add("Validation error.");
		}
	}
	public void clearDesign(){
		this.usersSelectedToShuffle.clear();
		try {
			seamDao.refresh(entity);
			this.entity.setDesignType(null);
			for (ProcessDefinition p : entity.getProcessDefinitions()) {
				for (UserAssignment ua : p.getUserAssignments()) {
					seamDao.remove(ua);
				}
				p.getUserAssignments().clear();
			}
			seamDao.merge(entity);
			seamDao.flush();
		} catch (ValidationException e) {
			getFacesMessages().add("Validation error.");
		}
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
	public void uploadDesignConfiguration(UploadEvent event) throws Exception {
		seamDao.refresh(this.entity);
		UploadItem item = event.getUploadItem();
		DesignConfigurationManager design = new DesignConfigurationManager(item.getFile().getAbsolutePath(), this.entity);
		this.entity = design.executeTransformations(this.entity);
		
		for (UserAssignment ua : this.entity.getAllUserAssignments()) {
			seamDao.persist(ua);
		}
		
		seamDao.merge(this.entity);
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

	@Override
	protected Workflow getExampleForFind() {
		return new Workflow();
	}

	public ProcessDefinition getProcessDefinitionProperty() {
		return processDefinitionProperty;
	}

	public void setProcessDefinitionProperty(ProcessDefinition processDefinitionProperty) {
		this.processDefinitionProperty = processDefinitionProperty;
	}

	public String getGroupProperty() {
		return groupProperty;
	}

	public void setGroupProperty(String groupProperty) {
		this.groupProperty = groupProperty;
	}

	public Map<String,List<User>> getUsersSelectedToShuffle() {
		return usersSelectedToShuffle;
	}

	public void setUsersSelectedToShuffle(Map<String,List<User>> usersSelectedToShuffle) {
		this.usersSelectedToShuffle = usersSelectedToShuffle;
	}

}
