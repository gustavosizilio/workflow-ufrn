package org.domain.workflow.session;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.domain.dao.SeamDAO;
import org.domain.dao.UserDAO;
import org.domain.dataManager.DesignConfigurationManager;
import org.domain.dataManager.WorkflowManager;
import org.domain.dsl.JPDLDSLUtil;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.DepVariable;
import org.domain.model.processDefinition.DesignType;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;
import org.domain.model.processDefinition.metric.Questionnaire;
import org.domain.utils.MailGun;
import org.domain.utils.PathBuilder;
import org.jboss.seam.ScopeType;
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
	@In("user") protected User user;
	@In(create=true, value="sendFile") protected SendFile sendFile;
	@In(value = "pathBuilder", create = true) PathBuilder pathBuilder;
	
	private Workflow entity;
	private Map<String,List<User>> usersSelectedToShuffle;
	private User userProperty;
	private User newUser;
	private String userPropertyString;
	private String groupProperty;
	private ProcessDefinition processDefinitionProperty;
	private Artefact currentArtefact;
	private boolean showModalAddUser;
	
	public void prepare(Workflow workflow) {
		this.userPropertyString = "";
		//this.setEntity(workflow);
		this.entity = seamDao.find(Workflow.class, workflow.getId());
		this.setUsersSelectedToShuffle(new Hashtable<String,List<User>>());
	}
	
	public List<User> autocomplete(Object suggest) {
        String pref = (String) suggest;
        List<User> users = userDAO.findAllByNameOrEmail(pref);
        return users;
    }
	
	public void updateUserProperty(User u) {
		this.userProperty = u;
	}
	
	public void downloadMetricsSheet(Workflow w) {
		this.prepare(w);
		downloadMetricsSheet();
	}
	public void downloadMetricsSheet() {
		try {
			Workbook wb = new HSSFWorkbook();
		    //Workbook wb = new XSSFWorkbook();
		    CreationHelper createHelper = wb.getCreationHelper();
		    Sheet sheet = wb.createSheet("Metrics");
		    Drawing drawing = sheet.createDrawingPatriarch();
		    
		    
		    List<UserAssignment> uas = this.getEntity().getAllUserAssignments();  
		    int col = 0;
		    for (int j=0; j<uas.size(); j++) {
		    	// Create a row and put some cells in it. Rows are 0 based.
			    Row row2 = sheet.createRow((short)j+1);
			    
			    // Create a cell and put a value in it.
			    List<String> factors = new ArrayList<String>();
			    factors.add(uas.get(j).getUser().toString());
			    
			    if(uas.get(j).getKeyFactors() != null && !uas.get(j).getKeyFactors().isEmpty()) {
			    	factors.addAll(Arrays.asList(uas.get(j).getKeyFactors().split("/")));
			    }
			    
			    col = factors.size();
			    for (int i = 0; i < col; i++) {
					String string = factors.get(i);
					Cell cell = row2.createCell(i);
					CellStyle style = wb.createCellStyle();
				    Font font = wb.createFont();
			        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			        style.setFont(font);
			        cell.setCellStyle(style);
				    cell.setCellValue(string);
				    
				    if(i==0) {
					    // Create the comment and set the text+author
					    ClientAnchor anchor = createHelper.createClientAnchor();
					    anchor.setCol1(cell.getColumnIndex());
					    anchor.setCol2(cell.getColumnIndex()+3);
					    anchor.setRow1(row2.getRowNum());
					    anchor.setRow2(row2.getRowNum()+7);
					    Comment comment = drawing.createCellComment(anchor);
					    comment.setString(createHelper.createRichTextString(uas.get(j).toString()));
	
					    // Assign the comment to the cell
					    cell.setCellComment(comment);
				    }
				    
				    sheet.autoSizeColumn(i);
				}
			    
			}
		    
		    
		    Row row = sheet.createRow((short)0);
		    List<DepVariable> vars = this.getEntity().getPlan().getDepVariables();
		    for (int j=0; j<vars.size();j++ ) {
				Cell cell = row.createCell(j+col);
				CellStyle style = wb.createCellStyle();
			    Font font = wb.createFont();
		        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		        style.setFont(font);
		        cell.setCellStyle(style);
			    cell.setCellValue(createHelper.createRichTextString(vars.get(j).getName()));
			    sheet.autoSizeColumn(j+col);
			    
			    if(!vars.get(j).getRange().isEmpty()) {
			    	CellRangeAddressList addressList = new CellRangeAddressList(1, 999, j+col, j+col);
					DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(vars.get(j).getRange().split(";"));
					DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
					dataValidation.setSuppressDropDownArrow(false);
					sheet.addValidationData(dataValidation);
			    }
			}
		    
	
		    // Write the output to a file
		    String file = pathBuilder.getExperimentMetricsSheetPath(this.getEntity());
		    FileOutputStream fileOut = new FileOutputStream(file);
		    
			wb.write(fileOut);
			wb.close();
			fileOut.close();
			
			sendFile.sendFile(file);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public void deployWorkflows() throws Exception {
		try {
			String experimentJpdlPath = pathBuilder.getExperimentJpdlPath(this.getEntity());
			JPDLDSLUtil.getInstance(pathBuilder.getJbossLibPath()).convertXMIToJPDL(pathBuilder.getExperimentXMIPath(this.getEntity()), experimentJpdlPath);
			
			//deploy jpdl
			WorkflowManager manager = new WorkflowManager(experimentJpdlPath, this.getEntity(),seamDao);
			manager.executeTransformations();
			seamDao.merge(this.getEntity());
			seamDao.flush();
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, e.getMessage());
			e.printStackTrace();
		}
		
		try {
			String experimentPath = pathBuilder.getExperimentPath(this.getEntity());
			String experimentConfPath = pathBuilder.getExperimentConfPath(this.getEntity());
			
			File f = new File(experimentConfPath); //delete the file to fix bug in the acceleo convertion that concat the content instead of recreate the file
			if (f.exists()) f.delete();
			
			JPDLDSLUtil.getInstance(pathBuilder.getJbossLibPath()).convertXMIToConf(pathBuilder.getExperimentXMIPath(this.getEntity()), experimentPath);
			deployDesignConfiguration(experimentConfPath);
			
		} catch (Exception e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void deployDefaultDesignConfiguration() {
		String experimentConfPath = pathBuilder.getExperimentConfPath(this.getEntity());
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
			seamDao.flush();
		} catch(Exception e){
			facesMessages.add(Severity.ERROR, "Failed to import design");
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}
	
	public void clearDesign(Boolean flush){
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
				seamDao.merge(p);
			}
			if(flush)
				seamDao.flush();
		} catch (ValidationException e) {
			facesMessages.add(Severity.ERROR,"Validation error.");
			e.printStackTrace();
		}
	}
	

	public void undeployWorkflow() {
		clearDesign(false);
		this.getEntity().setStartedAt(null);
		for (ProcessDefinition process : getEntity().getProcessDefinitions()) {
			seamDao.remove(process);
		}
		for (Questionnaire quest : getEntity().getQuestionnaires()) {
			seamDao.remove(quest);
		}
		getEntity().getQuestionnaires().clear();
		getEntity().getProcessDefinitions().clear();
		seamDao.flush();
		seamDao.refresh(getEntity());
		facesMessages.add("Undeploy efetuado com sucesso");
		new File(pathBuilder.getExperimentDataPath(getEntity()));
	}
	
	public void addUserManual(ActionEvent evt) throws ValidationException{
		seamDao.refresh(getEntity());
		
		tryGerUserProperty();
		
		if(this.userProperty != null) {
			if(getProcessDefinitionProperty() != null && !getProcessDefinitionProperty().getUsers().contains(userProperty)
					&& getEntity().isManualDesign() ){
				UserAssignment userAssignment = new UserAssignment(userProperty, getProcessDefinitionProperty(), null);
				seamDao.persist(userAssignment);
				seamDao.refresh(getProcessDefinitionProperty());
	
				getProcessDefinitionProperty().getUserAssignments().add(userAssignment);
				seamDao.merge(getProcessDefinitionProperty());
				
				seamDao.flush();
				userProperty = null;
				this.userPropertyString = null;
				processDefinitionProperty = null;
			}			
		}
	}

	private void tryGerUserProperty() {
		if(this.userProperty == null && this.userPropertyString != null && !this.userPropertyString.isEmpty()) {
			List<User> us = userDAO.findAllByNameOrEmail(this.userPropertyString);
			if(us.size() == 1) {
				this.userProperty = us.get(0);
			} else {
				this.newUser = new User();
				if(this.userPropertyString.contains("@")) {
					this.newUser.setEmail(this.userPropertyString);
				} else {
					this.newUser.setName(this.userPropertyString);					
				}
				this.setShowModalAddUser(true);
			}
		}
	}
	
	public void inviteNewUser() {
		List<User> u = userDAO.findAllByEmail(this.newUser.getEmail());
		if(u.size() > 0) {
			facesMessages.add(Severity.ERROR,"This user is already registered");
			return;
		}
		if(this.newUser.getName().trim().isEmpty()) {
			facesMessages.add(Severity.ERROR,"You should inform the name of the user");
			return;
		}
		
		String EMAIL_PATTERN = 
				"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(this.newUser.getEmail());
		
		if(!matcher.matches()) {
			facesMessages.add(Severity.ERROR,"You should inform a valid e-mail");
			return;
		}
		
		String passwordString = UUID.randomUUID().toString();
		passwordString = passwordString.split("-")[0];
		
		this.newUser.setPassword(passwordString);
		
		try {
			userDAO.persist(this.newUser);
			this.userProperty = this.newUser;
			this.userPropertyString = this.newUser.toString();
			this.showModalAddUser = false;
			
			String mailMsg = "Hello "+this.newUser.getName()+", the user "+user.getName()+" invited you to be a participant in an experiment. \n\n "
					+ "Access "+ pathBuilder.getWebPath() + "' Experiment Executer</a> using  the password "+passwordString;
			MailGun.sendMail(this.newUser.getEmail(), this.newUser.getName(), "You are invited for Experiment Executer", mailMsg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void addUserToShuffle(ActionEvent evt) throws ValidationException{
		
		tryGerUserProperty();
		
		if(this.userProperty != null) {
			if(this.getEntity().isRCBDDesign()){
				addUserToShuffleRCBD();
			}else if(this.getEntity().isLSDesign()){
				addUserToShuffleLS();
			}else if (this.getEntity().isCRDesign()){
				addUserToShuffleCRD();
			}
			this.userPropertyString = null;
			this.userProperty = null;
		}		
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
		
		String path = pathBuilder.getArtefactsPath(this.getEntity(), this.currentArtefact);
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
			new File(artefactFile.getFile()).delete();
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

	public String getUserPropertyString() {
		return userPropertyString;
	}

	public void setUserPropertyString(String userPropertyString) {
		this.userPropertyString = userPropertyString;
	}

	public boolean isShowModalAddUser() {
		return showModalAddUser;
	}

	public void setShowModalAddUser(boolean showModalAddUser) {
		this.showModalAddUser = showModalAddUser;
	}

	public User getNewUser() {
		return newUser;
	}

	public void setNewUser(User newUser) {
		this.newUser = newUser;
	}
	
}
