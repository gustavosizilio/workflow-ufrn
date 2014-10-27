package org.domain.workflow.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.apache.commons.beanutils.BeanUtils;
import org.domain.dao.UserDAO;
import org.domain.dao.WorkflowDAO;
import org.domain.dataManager.DesignConfigurationManager;
import org.domain.dataManager.WorkflowManager;
import org.domain.dsl.DSLUtil;
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
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;
import org.richfaces.model.UploadItem;

import br.ufrn.dimap.ase.dsl.expdslv3.Task;


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
	private DSLUtil dslUtil;
	private EObject rootModel;
	private TreeNode<EObject> rootNode;
	private TreeNode<EObject> editNode;
	
	
	private String textProperty;
	private int intProperty;
	private Enumerator enumProperty;
	private Object refAttrProperty;
	
	
	public CrudWorkflow() throws Exception {
		super(Workflow.class);
		this.setUsersSelectedToShuffle(new Hashtable<String,List<User>>());
		dslUtil = DSLUtil.getInstance();
	}
	
	@Override
	public void findEntities()
	{
		setEntities(this.workflowDAO.findAllByUser(user));
	}
	
	@Override
	protected void createImpl(){
		clearEditProperties();
		this.entity.setUser(user);
		this.rootModel = dslUtil.getRootElement();
		rootNode = new TreeNodeImpl<EObject>();
		updateTreeNode();
	}
	
	@Override
	protected boolean saveImpl(){
		try {
			this.entity.setDefinition(transform(this.rootModel));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.saveImpl();
	}
	
	@Override
	protected void editImpl(){
		try {
			this.rootModel = transform(this.entity.getDefinition());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	public EObject transform(byte[] barray) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = null;
		JBossObjectInputStream objIn = null;
		EObject model = null;
		try {
			in = new ByteArrayInputStream(barray);
			objIn = new JBossObjectInputStream(in);
			model = (EObject) objIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(objIn != null)
					objIn.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return model;
	}
	
	public byte[] transform(EObject model) throws IOException {
		ByteArrayOutputStream out = null;
		JBossObjectOutputStream objOut = null;
		try {
			out = new ByteArrayOutputStream();
			objOut = new JBossObjectOutputStream(out);
			objOut.writeObject(model);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(objOut != null)
					objOut.close();
				if(out != null)
					out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out.toByteArray();
	}
	
	private void updateTreeNode() {
		TreeNodeImpl<EObject> node = new TreeNodeImpl<EObject>();
		node.setData(this.rootModel);
		rootNode.addChild(this.rootModel, node);
		for (EObject eObject : this.rootModel.eContents()) {
			updateTreeNode(eObject, node);
		}
	}
	private void updateTreeNode(EObject eObject, TreeNode<EObject> rootNode) {
		TreeNodeImpl<EObject> node = new TreeNodeImpl<EObject>();
		node.setData(eObject);
		rootNode.addChild(eObject, node);
		
		List<EObject> attrs = dslUtil.getAttrs(eObject);
		for (EObject attr : attrs) {
			TreeNodeImpl<EObject> attrNode = new TreeNodeImpl<EObject>();
			attrNode.setData(attr);
			node.addChild(attr, attrNode);
		}
		
		for (EObject o : eObject.eContents()) {
			updateTreeNode(o, node);
		}
	}
	
	public Object getValueFromParent(TreeNode<EObject> node) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return dslUtil.getValue(node.getParent().getData(), node.getData());
	}
	
	public String getFieldType(TreeNode<EObject> node) {
		if(node.getData() instanceof EReference) {
			if(((EReference)node.getData()).isMany()) {
				return "refAttrMany";
			} else {
				return "refAttrOne";
			}
		} else if(node.getData() instanceof EAttribute) {
			if(((EAttribute)node.getData()).getEType().getInstanceTypeName() == "java.lang.String" ){
				return "string";
			} else if(((EAttribute)node.getData()).getEType().getInstanceTypeName() == "int") {
				return "int";
			} else {
				if(((EAttribute)node.getData()).getEType() instanceof EEnum) {
					return "enum";
				}
			}
		}
		return "";
	}
	
	//passar para o dslutil
	public List<EObject> getFieldValues(EObject eObject){
		return dslUtil.getRefValues(this.rootModel, eObject);
	}

	public void updateEditProperty(TreeNode<EObject> node) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		clearEditProperties();
		this.editNode = node;
		Object value = getValueFromParent(node);
		
		String type = getFieldType(node);
		
		if(value != null) {
			if(type.equals("refAttr") ){
				this.setRefAttrProperty(value);
			} else if(type.equals("string") ){
				this.setTextProperty(value.toString());
			} else if(type.equals("int")) {
				this.setIntProperty(Integer.parseInt(value.toString()));
			} else if(type.equals("enum")) {
				this.setEnumProperty((Enumerator) value);
			}
		}
	}
	public void updateEditProperty() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String type = getFieldType(this.editNode);
		if(type.equals("refAttr") ){
			BeanUtils.setProperty(this.editNode.getParent().getData(), ((EReference)this.editNode.getData()).getName(), this.getRefAttrProperty());
		} else if(type.equals("string") ){
			BeanUtils.setProperty(this.editNode.getParent().getData(), ((EAttribute)this.editNode.getData()).getName(), this.getTextProperty());
		} else if(type.equals("int")) {
			BeanUtils.setProperty(this.editNode.getParent().getData(), ((EAttribute)this.editNode.getData()).getName(), this.getIntProperty());
		} else if(type.equals("enum")) {
			BeanUtils.setProperty(this.editNode.getParent().getData(), ((EAttribute)this.editNode.getData()).getName(), this.getEnumProperty());
		}
		
		clearEditProperties();
	}

	private void clearEditProperties() {
		this.editNode = null;
		this.setTextProperty("");
		this.setIntProperty(0);
	}

	public String getItemType(EObject eObject) {
		if(eObject instanceof EAttribute) {
			return "attr";
		} else if (eObject instanceof EReference) {
			if(((EReference)eObject).isContainment()) {
				return "ref";
			} else {
				return "attr";
			}
		}
		return "ref";
	}
	
	public String getName(String s) {
		String className = s;
		String[] r = className.split("(?=\\p{Lu})");
		StringBuilder result = new StringBuilder();
		for (String string : r) {
			result.append(string + " ");
		}
		
		String ret = result.toString();
		if(ret.toLowerCase() == "model") {
			return "Experiment";
		} else {
			return ret;
		}
	}
	
	public String getName(EObject eObject) {
		if(eObject instanceof EAttribute) {
			return ((EAttribute) eObject).getName();
		} else if(eObject instanceof EReference && !((EReference)eObject).isContainment()) {
			return ((EReference) eObject).getName();
		} else {
			String className = eObject.getClass().getInterfaces()[0].toString();
			className = className.substring(className.lastIndexOf(".")+1);
			return getName(className);
		}
	}
	
	public void  buildRef(EObject raiz, EReferenceImpl ref, EClass refClass) {
		try {
			this.dslUtil.buildRef(raiz, ref, refClass);
			this.updateTreeNode();
		} catch (Exception e) {
			getFacesMessages().add(e.getMessage());
		}
	}
	
	//TODO parse jpdl
	public void deployWorkflow(UploadEvent event) throws Exception {
	    UploadItem item = event.getUploadItem();
	    WorkflowManager jpdl = new WorkflowManager(item.getFile().getAbsolutePath(), this.entity, this.seamDao);
	    try {
			jpdl.executeTransformations();
		} catch (ValidationException e) {
			addErrors(e.getErrors());
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String undeployWorkflow() {
		clearDesign();
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
		if(this.entity.isRCBDDesign()){
			addUserToShuffleRCBD();
		}else if(this.entity.isLSDesign()){
			addUserToShuffleLS();
		}else if (this.entity.isCRDesign()){
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
		if(entity.isCRDesign()){
			ArrayList<String> r = new ArrayList<String>();
			r.add("Subjects");
			return r;
		}
		return new ArrayList<String>(entity.getGroups());
	}
	public void suffleUsersBlock() throws ValidationException{
		boolean hasError = false;
		for (String groupValue : getGroupValues()) {
			if (this.getUsersSelectedToShuffle().get(groupValue) == null){
				getFacesMessages().add("Incomplete configuration");
				hasError = true;
			} else if(this.getUsersSelectedToShuffle().get(groupValue).size() < this.entity.getQuantityOfSubjectsNeeds(groupValue)){
				getFacesMessages().add("User quantity is not enought for the group "+groupValue);
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
	public void shuffleUsersRCDB() throws ValidationException{
		suffleUsersBlock();
	}
	public void shuffleUsersLS() throws ValidationException{
		boolean hasError = false;
		for (String groupValue : getGroupValues()) {
			if(this.getUsersSelectedToShuffle().get(groupValue) == null || this.getUsersSelectedToShuffle().get(groupValue).size() < this.entity.getQuantityOfSubjectsNeeds(groupValue)){
				getFacesMessages().add("User quantity is not enought for the group "+groupValue);
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
	public void shuffleUsersCRD() throws ValidationException{
		suffleUsersBlock();
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
	
	public void start(Workflow w) throws ValidationException{
		seamDao.refresh(w);
		w.setStartedAt(Calendar.getInstance().getTime());
		w.nextTurn();
		seamDao.merge(w);
		seamDao.flush();
	}
	public void nextTurn(Workflow w) throws ValidationException{
		seamDao.refresh(w);
		w.nextTurn();
		seamDao.merge(w);
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
			this.entity.setCurrentTurn(null);
			this.entity.setTurnQuantity(null);
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
	public void uploadDesignConfiguration(UploadEvent event) {
		try{
			seamDao.refresh(this.entity);
			UploadItem item = event.getUploadItem();
			DesignConfigurationManager design = new DesignConfigurationManager(item.getFile().getAbsolutePath(), this.entity);
			this.entity = design.executeTransformations(this.entity);
		
			for (UserAssignment ua : this.entity.getAllUserAssignments()) {
				seamDao.persist(ua);
			}
			seamDao.merge(this.entity);
		} catch(Exception e){
			getFacesMessages().add("Erro ao importar design");
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

	public DSLUtil getDslUtil() {
		return dslUtil;
	}

	public void setDslUtil(DSLUtil dslUtil) {
		this.dslUtil = dslUtil;
	}

	public TreeNode<EObject> getRootNode() {
		return rootNode;
	}

	public void setRootNode(TreeNode<EObject> rootNode) {
		this.rootNode = rootNode;
	}

	public String getTextProperty() {
		return textProperty;
	}

	public void setTextProperty(String textProperty) {
		this.textProperty = textProperty;
	}

	public TreeNode<EObject> getEditNode() {
		return editNode;
	}

	public void setEditNode(TreeNode<EObject> editNode) {
		this.editNode = editNode;
	}
	
	public void doNothing(){
		System.err.println("do nothing...");
	}

	public int getIntProperty() {
		return intProperty;
	}

	public void setIntProperty(int intProperty) {
		this.intProperty = intProperty;
	}

	public Enumerator getEnumProperty() {
		return enumProperty;
	}

	public void setEnumProperty(Enumerator enumProperty) {
		this.enumProperty = enumProperty;
	}

	public Object getRefAttrProperty() {
		return refAttrProperty;
	}

	public void setRefAttrProperty(Object refAttrProperty) {
		this.refAttrProperty = refAttrProperty;
	}

}
