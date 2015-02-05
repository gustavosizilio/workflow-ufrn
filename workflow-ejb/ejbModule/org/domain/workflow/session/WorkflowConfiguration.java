package org.domain.workflow.session;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.domain.dao.SeamDAO;
import org.domain.dao.UserDAO;
import org.domain.dataManager.DesignConfigurationManager;
import org.domain.dataManager.WorkflowManager;
import org.domain.dsl.JPDLDSLUtil;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.DesignType;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;
import org.domain.utils.PathBuilder;
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

@Name("configuration")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowConfiguration {
	@In("seamDao") protected SeamDAO seamDao;
	@In private FacesMessages facesMessages;
	@In("userDao") UserDAO userDAO;
	
	private Workflow entity;
	private Map<String,List<User>> usersSelectedToShuffle;
	private User userProperty;
	private String groupProperty;
	private ProcessDefinition processDefinitionProperty;
	private Artefact currentArtefact;
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
	public void prepare(Workflow workflow) {
		this.setEntity(workflow);
		this.setUsersSelectedToShuffle(new Hashtable<String,List<User>>());
	}
	
	public void deployWorkflows() throws Exception {
		try {
			String experimentJpdlPath = PathBuilder.getExperimentJpdlPath(this.getEntity());
			JPDLDSLUtil.getInstance().convertXMIToJPDL(PathBuilder.getExperimentXMIPath(this.getEntity()), experimentJpdlPath);
			
			//deploy jpdl
			WorkflowManager manager = new WorkflowManager(experimentJpdlPath, this.getEntity(),seamDao);
			manager.executeTransformations();
			seamDao.merge(this.getEntity());
			seamDao.flush();
		} catch (Exception e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
		
		try {
			String experimentPath = PathBuilder.getExperimentPath(this.getEntity());
			String experimentConfPath = PathBuilder.getExperimentConfPath(this.getEntity());
			
			File f = new File(experimentConfPath); //delete the file to fix bug in the acceleo convertion that concat the content instead of recreate the file
			if (f.exists()) f.delete();
			
			JPDLDSLUtil.getInstance().convertXMIToConf(PathBuilder.getExperimentXMIPath(this.getEntity()), experimentPath);
			deployDesignConfiguration(experimentConfPath);
			
		} catch (Exception e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void deployDefaultDesignConfiguration() {
		String experimentConfPath = PathBuilder.getExperimentConfPath(this.getEntity());
		deployDesignConfiguration(experimentConfPath);
	}
	
	public void deployDesignConfiguration(String path) {
		try{
			seamDao.refresh(this.getEntity());
			DesignConfigurationManager design = new DesignConfigurationManager(path, this.getEntity());
			this.setEntity(design.executeTransformations(this.getEntity()));
		
			for (UserAssignment ua : this.getEntity().getAllUserAssignments()) {
				seamDao.persist(ua);
			}
			seamDao.merge(this.getEntity());
		} catch(Exception e){
			facesMessages.add(Severity.ERROR, "Failed to import design");
		}
	}
	
	public void updateDesignTypeToManual(){
		try {
			seamDao.refresh(this.getEntity());
			this.getEntity().setDesignType(DesignType.MANUAL);
			seamDao.merge(this.getEntity());
			seamDao.flush();
		} catch (ValidationException e) {
			facesMessages.add(Severity.ERROR,"Validation error.");
		}
	}
	
	public void clearDesign(){
		this.usersSelectedToShuffle.clear();
		try {
			seamDao.refresh(getEntity());
			this.getEntity().setCurrentTurn(null);
			this.getEntity().setTurnQuantity(null);
			this.getEntity().setDesignType(null);
			for (ProcessDefinition p : getEntity().getProcessDefinitions()) {
				for (UserAssignment ua : p.getUserAssignments()) {
					seamDao.remove(ua);
				}
				p.getUserAssignments().clear();
			}
			seamDao.merge(getEntity());
			seamDao.flush();
		} catch (ValidationException e) {
			facesMessages.add(Severity.ERROR,"Validation error.");
		}
	}
	

	public void undeployWorkflow() {
		clearDesign();
		for (ProcessDefinition process : getEntity().getProcessDefinitions()) {
			seamDao.remove(process);
		}
		getEntity().getProcessDefinitions().clear();
		seamDao.flush();
		seamDao.refresh(getEntity());
		facesMessages.add("Undeploy efetuado com sucesso");
	}
	
	public void addUserToShuffle(ActionEvent evt) throws ValidationException{
		if(this.getEntity().isRCBDDesign()){
			addUserToShuffleRCBD();
		}else if(this.getEntity().isLSDesign()){
			addUserToShuffleLS();
		}else if (this.getEntity().isCRDesign()){
			addUserToShuffleCRD();
		}
		this.userProperty = null;
	}
	
	private void addUserToShuffleCRD() {
		if(this.groupProperty == null)
			this.groupProperty = "Subjects";
		addUserToShuffleBlock();
	}
	private void addUserToShuffleRCBD(){
		addUserToShuffleBlock();
	}
	private void addUserToShuffleLS(){
		addUserToShuffleBlock();
	}
	private void addUserToShuffleBlock() {
		if(this.userProperty != null && this.groupProperty != null && !isUserPresentToShuffle(this.userProperty)){
			if(!this.getUsersSelectedToShuffle().containsKey(this.groupProperty)){
				this.getUsersSelectedToShuffle().put(this.groupProperty, new ArrayList<User>());
			}
			this.getUsersSelectedToShuffle().get(this.groupProperty).add(this.userProperty);
		}
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
//		List<String> groups = new ArrayList<String>();
//		for (String string : this.getUsersSelectedToShuffle().keySet()) {
//			groups.add(string);
//		}
//		return groups;
		if(getEntity().isCRDesign()){
			ArrayList<String> r = new ArrayList<String>();
			r.add("Subjects");
			return r;
		}
		return new ArrayList<String>(getEntity().getGroups());
	}
	
	public void suffleUsersBlock() throws ValidationException{
		boolean hasError = false;
		for (String groupValue : getGroupValues()) {
			if (this.getUsersSelectedToShuffle().get(groupValue) == null){
				facesMessages.add(Severity.ERROR,"Incomplete configuration");
				hasError = true;
			} else if(this.getUsersSelectedToShuffle().get(groupValue).size() < this.getEntity().getQuantityOfSubjectsNeeds(groupValue)){
				facesMessages.add(Severity.ERROR,"User quantity is not enought for the group "+groupValue);
				hasError = true;
			}
		}
		if(!hasError){
			for (String groupValue : getGroupValues()) {
				List<User> users = this.getUsersSelectedToShuffle().get(groupValue);
				Collections.shuffle(users);
				for (User user : users) {
					this.getEntity().addUserToGroup(groupValue, user);
				}
			}
		}
		
		seamDao.merge(this.getEntity());
		seamDao.flush();
	}
	
	public void shuffleUsersRCDB() throws ValidationException{
		suffleUsersBlock();
	}
	
	public void shuffleUsersLS() throws ValidationException{
		boolean hasError = false;
		for (String groupValue : getGroupValues()) {
			if(this.getUsersSelectedToShuffle().get(groupValue) == null || this.getUsersSelectedToShuffle().get(groupValue).size() < this.getEntity().getQuantityOfSubjectsNeeds(groupValue)){
				facesMessages.add(Severity.ERROR,"User quantity is not enought for the group "+groupValue);
				hasError = true;
			}
		}
		if(!hasError){
			for (String groupValue : getGroupValues()) {
				List<User> users = this.getUsersSelectedToShuffle().get(groupValue);
				Collections.shuffle(users);
				for (User user : users) {
					this.getEntity().addUserToGroup(groupValue, user);
				}
			}
		}
		
		seamDao.merge(this.getEntity());
		seamDao.flush();
	}
	public void shuffleUsersCRD() throws ValidationException{
		suffleUsersBlock();
	}
	
	public void addUserManual(ActionEvent evt) throws ValidationException{
		seamDao.refresh(getEntity());
		if(userProperty != null && getProcessDefinitionProperty() != null && !getProcessDefinitionProperty().getUsers().contains(userProperty)
				&& getEntity().isManualDesign() ){
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
		if(getEntity().isManualDesign()  ){
			seamDao.refresh(userAssignment);
			seamDao.remove(userAssignment);
			
			processDefinitionProperty = userAssignment.getProcessDefinition();
			userProperty = userAssignment.getUser();
			
			processDefinitionProperty.getUserAssignments().remove(userAssignment);
			seamDao.merge(processDefinitionProperty);
			
			seamDao.flush();
		}
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

	public Map<String,List<User>> getUsersSelectedToShuffle() {
		return usersSelectedToShuffle;
	}

	public void setUsersSelectedToShuffle(Map<String,List<User>> usersSelectedToShuffle) {
		this.usersSelectedToShuffle = usersSelectedToShuffle;
	}
	
	public User getUserProperty() {
		return userProperty;
	}

	public void setUserProperty(User userProperty) {
		this.userProperty = userProperty;
	}
	
	public String getGroupProperty() {
		return groupProperty;
	}

	public void setGroupProperty(String groupProperty) {
		this.groupProperty = groupProperty;
	}
	
	public ProcessDefinition getProcessDefinitionProperty() {
		return processDefinitionProperty;
	}

	public void setProcessDefinitionProperty(ProcessDefinition processDefinitionProperty) {
		this.processDefinitionProperty = processDefinitionProperty;
	}
	
	public void setCurrentArtefact(Artefact artefact){
		this.currentArtefact = artefact;
	}

	public Workflow getEntity() {
		return entity;
	}

	public void setEntity(Workflow entity) {
		this.entity = entity;
	}
	
}
