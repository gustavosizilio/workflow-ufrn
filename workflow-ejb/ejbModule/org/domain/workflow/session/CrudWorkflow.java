package org.domain.workflow.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
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
	private TreeNode<EObject> selectedNode;
	private EObject newNode;
	private List<Object[]> attrProperties;
	
	
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
		this.setRootModel(dslUtil.getRootElement());
		this.rootNode = new TreeNodeImpl<EObject>();
		updateTreeNode(null);
		this.selectedNode = this.rootNode.getChild(this.getRootModel());
	}
	
	@Override
	protected void editImpl(){
		try {
			clearEditProperties();
			this.setRootModel(transform(this.entity.getDefinition()));
			this.rootNode = new TreeNodeImpl<EObject>();
			updateTreeNode(null);
			this.selectedNode = this.rootNode.getChild(this.getRootModel());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected boolean saveImpl(){
		try {
			this.entity.setDefinition(transform(this.getRootModel()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.saveImpl();
	}
	
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
	
	private void updateTreeNode(EObject newNode) {
		TreeNodeImpl<EObject> node = new TreeNodeImpl<EObject>();
		node.setData(this.getRootModel());
		rootNode.addChild(this.getRootModel(), node);
		for (EObject eObject : this.getRootModel().eContents()) {
			updateTreeNode(eObject, node, newNode);
		}
	}
	
	private void updateTreeNode(EObject eObject, TreeNode<EObject> rootNode, EObject newNode) {
		TreeNodeImpl<EObject> node = new TreeNodeImpl<EObject>();
		node.setData(eObject);
		
		if(newNode == eObject) {
			this.selectedNode = node;
		}
		
		rootNode.addChild(eObject, node);
		List<EObject> attrs = dslUtil.getAttrs(eObject);
		for (EObject attr : attrs) {
			TreeNodeImpl<EObject> attrNode = new TreeNodeImpl<EObject>();
			attrNode.setData(attr);
			node.addChild(attr, attrNode);
		}
		
		for (EObject o : eObject.eContents()) {
			updateTreeNode(o, node, newNode);
		}
		
		this.setNewNode(newNode);
	}
	
	public List<TreeNode<EObject>> getChildren(TreeNode<EObject> node, EReference ref) {
		List<TreeNode<EObject>> children = new ArrayList<TreeNode<EObject>>();
		List<EObject> refObjs = getRefsObjects(node.getData(), ref);
		
		Iterator<Entry<Object, TreeNode<EObject>>> it = node.getChildren();
		while (it.hasNext()) {
			TreeNode<EObject> o = it.next().getValue();
			if(refObjs.contains(o.getData())){
				children.add(o);
			}
		}
		return children;
	}
	
	public List<EReference>  getRefs(EObject raiz){
		return this.dslUtil.getRefs(raiz);
	} 
	
	public List<EObject>  getRefsObjects(EObject raiz, EReference ref){
		return this.dslUtil.getRefsObjects(raiz, ref);
	} 
	
	public List<Object[]>  getAttrs(EObject raiz){
		//string int enum refAttrMany refAttrOne
		 List<EObject> attrs = this.dslUtil.getAttrs(raiz);
		 this.attrProperties = new ArrayList<Object[]>();
		 for (EObject object : attrs) {
			 System.out.println(object);
			 attrProperties.add(new Object[] {object, getValueFromParent(this.selectedNode.getData(), object, false)});
		 }
		 return attrProperties;
	} 
	
	@SuppressWarnings("rawtypes")
	public Object getValueFromParent(EObject parent, EObject node, Boolean toString) {
		Object o = null;
		try {
			EObject e1 = parent ;
			EObject e2 = node;
			o = dslUtil.getValue(e1, (EStructuralFeature) e2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		if(o != null) {
			if(o instanceof EList<?>) {
				for (Object obj : (EList)o) {
					if(obj instanceof EObject) {
						sb.append(getName((EObject) obj, true)+"\n");
					} else {
						sb.append(obj.toString()+"\n");
					}
				}
			} else {
				sb.append(o.toString()+"\n");
			}
		}
		if(toString){
			return sb.toString();
		} else {
			return o;
		}
	}
	
	public String getFieldType(TreeNode<EObject> node) {
		return getFieldType(node.getData());
	}
	
	public String getFieldType(EObject node) {
		if(node instanceof EReference) {
			if(((EReference)node).isMany()) {
				return "refAttrMany";
			} else {
				return "refAttrOne";
			}
		} else if(node instanceof EAttribute) {
			if(((EAttribute)node).getEType().getInstanceTypeName() == "java.lang.String" ){
				return "string";
			} else if(((EAttribute)node).getEType().getInstanceTypeName() == "int") {
				return "int";
			} else {
				if(((EAttribute)node).getEType() instanceof EEnum) {
					if(((EAttribute)node).isMany()) {
						return "enumMany";
					} else {
						return "enumOne";
					}
				}
			}
		}
		return "none";
	}
	
	public List<EObject> getFieldValues(EObject eObject){
		return dslUtil.getRefValues(this.getRootModel(), eObject);
	}
	
	public void updateSelectedNode(TreeNode<EObject> node){
		clearEditProperties();
		this.selectedNode = node;
	}

	public void updateAttrProperties() throws Exception {
		if(attrProperties != null) {
			try {
				for (Object[] pair : attrProperties) {
					EObject refAttr = (EObject)pair[0];
					//BeanUtils.setProperty(refAttr.eContainer(), ((EAttribute)refAttr).getName(), pair[1]);
					dslUtil.setValue(this.selectedNode.getData(), (EStructuralFeature) refAttr, pair[1]);
				}
			} catch (Exception e){
				e.printStackTrace();
				clearEditProperties();
			}
		}
		
		clearEditProperties();
		this.updateSelectedNode(this.selectedNode.getParent());
		
	}

	public void clearEditProperties() {
		if(this.attrProperties != null) {
			this.attrProperties.clear();
		}
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
	
	public String getName(EObject eObject, boolean includeLookForName) {
		StringBuilder sb = new StringBuilder();
		if(eObject instanceof EAttribute) {
			sb.append(((EAttribute) eObject).getName());
		} else { 
			boolean lookForName = false;
			if(eObject instanceof EReference && !((EReference)eObject).isContainment()) {
				sb.append(((EReference) eObject).getName());
			} else {
				String className = eObject.getClass().getInterfaces()[0].toString();
				className = className.substring(className.lastIndexOf(".")+1);
				sb.append(getName(className));
				lookForName = true;
			}
			if(lookForName && includeLookForName) {
				try {
					String id = BeanUtils.getProperty(eObject, "id");
					if(!id.equals("id") && !id.isEmpty()  && id != null) {
						sb.append(" ["+id+"]");
					}
				} catch (Exception e) {
				}
				try {
					String name = BeanUtils.getProperty(eObject, "name");
					if(!name.equals("name") && !name.isEmpty()  && name != null) {
						sb.append(" ("+name+")");
					}
				} catch (Exception e) {
				}
			}
		}
		
		
		return sb.toString();
	}
	
	public void  buildRef(EObject raiz, EReferenceImpl ref, EClass refClass) {
		try {
			EObject o = this.dslUtil.buildRef(raiz, ref, refClass);
			this.updateTreeNode(o);
		} catch (Exception e) {
			e.printStackTrace();
			getFacesMessages().add(e.getMessage());
		}
	}
	
	public void  removeRef(TreeNodeImpl<EObject> raiz, EObject obj, EReferenceImpl ref) {
		try {
			this.dslUtil.removeRef(raiz.getData(), obj, ref);
			this.updateTreeNode(raiz.getParent().getData());
		} catch (Exception e) {
			e.printStackTrace();
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

	public void doNothing(){
		System.err.println("do nothing...");
	}

	public EObject getNewNode() {
		return newNode;
	}

	public void setNewNode(EObject newNode) {
		this.newNode = newNode;
	}

	public TreeNode<EObject> getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode<EObject> selectedNode) {
		this.selectedNode = selectedNode;
	}

	public EObject getRootModel() {
		return rootModel;
	}

	public void setRootModel(EObject rootModel) {
		this.rootModel = rootModel;
	}

}
