package org.domain.workflow.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.commons.beanutils.BeanUtils;
import org.domain.dao.WorkflowDAO;
import org.domain.dsl.EXPDSLUtil;
import org.domain.exception.ValidationException;
import org.domain.model.processDefinition.Workflow;
import org.domain.utils.PathBuilder;
import org.domain.workflow.session.generic.CrudAction;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.SeamResourceBundle;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;


@Name("crudWorkflow")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class CrudWorkflow extends CrudAction<Workflow> {
	
	@In("workflowDAO") WorkflowDAO workflowDAO;
	@In(create = true, required = false, value="configuration") WorkflowConfiguration workflowConfiguration;
	@In(value = "pathBuilder", create = true)  PathBuilder pathBuilder;
	@In(value = "expdslUtil", create = true) private EXPDSLUtil dslUtil;
	private EObject rootModel;
	private TreeNode<EObject> rootNode;
	private TreeNode<EObject> selectedNode;
	private EObject newNode;
	private List<Object[]> attrProperties;
	private EReferenceImpl selectedRef;
	private String myexpPath;
	private StringBuilder treeString;
	
	
	public CrudWorkflow() throws Exception {
		super(Workflow.class);
		treeString = new StringBuilder();
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
	protected void detailImpl(){
		workflowConfiguration.prepare(this.entity);
		try {
			clearEditProperties();
			
			String xmiPath = pathBuilder.getExperimentXMIPath(this.entity);
			this.rootModel = dslUtil.convertXMIToEcore(xmiPath);
			
			this.rootNode = new TreeNodeImpl<EObject>();
			updateTreeNode(null);
			this.selectedNode = this.rootNode.getChild(this.getRootModel());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			addError(e.getMessage());
		}
	}
		
	@Override
	protected void editImpl(){
		try {
			clearEditProperties();
			
			String xmiPath = pathBuilder.getExperimentXMIPath(this.entity);
			this.rootModel = dslUtil.convertXMIToEcore(xmiPath);
			
			this.rootNode = new TreeNodeImpl<EObject>();
			updateTreeNode(null);
			this.selectedNode = this.rootNode.getChild(this.getRootModel());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			addError(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	protected boolean saveImpl(){
		boolean ret = super.saveImpl();
		
		try {
			String xmiPath = pathBuilder.getExperimentXMIPath(this.entity);
			dslUtil.convertEcoreToXMI(this.rootModel, xmiPath);
		} catch (Exception e) {
			System.err.println("failed to transform model....");
			e.printStackTrace();
			addError(e.getMessage());
		}
		
		try {
			String experimentPath = pathBuilder.getExperimentMyexpPath(this.entity);
			dslUtil.convertEcoreToExpText(this.rootModel, experimentPath);
			this.entity.setSuccessCompiled(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			addError(e.getMessage());
			this.entity.setSuccessCompiled(false);
			e.printStackTrace();
		}
		
		
		try {
			this.seamDao.merge(this.entity);
		} catch (ValidationException e) {
			addError(e.getMessage());
			e.printStackTrace();
		}
		this.seamDao.flush();
		
		try {
			dslUtil.convertEcoreToImage(this.rootModel, pathBuilder.getExperimentPath(this.entity), pathBuilder.getExperimentImagePath(this.entity));
		} catch (Exception e) {
			e.printStackTrace();
			addError(e.getMessage());
		}

		return ret;
	}
	
	public String save(){
		if (saveImpl()){
			return detail(entity);
		} else {
			return getPage();
		}
	}
	
	public String getTreeString(){
		return treeString.toString();
	}
	
	private void updateTreeNode(EObject newNode) {
		treeString.setLength(0);
		TreeNodeImpl<EObject> node = new TreeNodeImpl<EObject>();
		node.setData(this.getRootModel());
		rootNode.addChild(this.getRootModel(), node);
		if(this.getRootModel() != null) {
			for (EObject eObject : this.getRootModel().eContents()) {
				updateTreeNode(eObject, node, newNode);
			}
		}
	}
	
	private void updateTreeNode(EObject eObject, TreeNode<EObject> rootNode, EObject newNode) {
		TreeNodeImpl<EObject> node = new TreeNodeImpl<EObject>();
		node.setData(eObject);
		treeString.append(getName(eObject, true) + "\n");
		
		if(newNode == eObject) {
			this.selectedNode = node;
		}
		
		rootNode.addChild(eObject, node);
		List<EObject> attrs = dslUtil.getAttrs(eObject);
		for (EObject attr : attrs) {
			TreeNodeImpl<EObject> attrNode = new TreeNodeImpl<EObject>();
			attrNode.setData(attr);
			node.addChild(attr, attrNode);
			
			treeString.append("\t" + getName(attr, true) + ": " + getValueFromParent(eObject, attr, true) + "\n");
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
	
	public boolean isId(EObject node) {
		if(node instanceof EAttribute) {
			return ((EAttribute) node).isID();
		}
		
		return false;
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
		this.selectedRef = null;
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
		//this.updateSelectedNode(this.selectedNode.getParent());
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
		if(ret.toLowerCase().trim().equals("model")) {
			return "Experiment";
		} else {
			return ret.trim();
		}
	}
	
	public String getHint(EObject eObject) {
		if(eObject == null)
			return null;
		
		try {
			String className = null;
			if(eObject instanceof EClassImpl) {
				className = ((EClassImpl)eObject).getName();
			} else if (eObject instanceof EAttributeImpl || eObject instanceof EReference ) {
				className = this.selectedNode.getData().getClass().getInterfaces()[0].toString();
				className = className.substring(className.lastIndexOf(".")+1);
				if(eObject instanceof EReference) {
					className = className + "." + ((EReference) eObject).getName();
				} else if (eObject instanceof EAttributeImpl) {
					className = className + "." + ((EAttributeImpl)eObject).getName();
				}
			} else {
				className = eObject.getClass().getInterfaces()[0].toString();
				className = className.substring(className.lastIndexOf(".")+1);
			}
			String hintKey = "layout.formalization.hint." + getName(className).replaceAll(" ", "");
			@SuppressWarnings("unused")
			Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			ResourceBundle resourceBundle = SeamResourceBundle.getBundle();
			String bundleMessage = resourceBundle.getString(hintKey);
			return bundleMessage;
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getName(EObject eObject, boolean includeLookForName) {
		if(eObject != null) {
			StringBuilder sb = new StringBuilder();
			if(eObject instanceof EAttribute) {
				sb.append(((EAttribute) eObject).getName());
			} else { 
				boolean lookForName = false;
				if(eObject instanceof EReference && !((EReference)eObject).isContainment()) {
					sb.append(((EReference) eObject).getName());
				} else {
					if(eObject.getClass() != null && eObject.getClass().getInterfaces() != null && eObject.getClass().getInterfaces()[0] != null) {
						String className = eObject.getClass().getInterfaces()[0].toString();
						className = className.substring(className.lastIndexOf(".")+1);
						sb.append(getName(className));
						lookForName = true;
					}
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
		} else {
			return "";
		}
	}
	
	public void  buildRef(EObject raiz, EReferenceImpl ref, EClass refClass) {
		try {
			EObject o = this.dslUtil.buildRef(raiz, ref, refClass);
			this.setSelectedRef(ref);
			this.updateTreeNode(o);
		} catch (Exception e) {
			e.printStackTrace();
			getFacesMessages().add(e.getMessage());
		}
	}
	
	public void  removeRef(TreeNodeImpl<EObject> raiz, EObject obj, EReferenceImpl ref) {
		try {
			this.dslUtil.removeRef(raiz.getData(), obj, ref);
			this.updateTreeNode(raiz.getData());
		} catch (Exception e) {
			e.printStackTrace();
			getFacesMessages().add(e.getMessage());
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
	public void uploadExpFile(UploadEvent event) {
		setMyexpPath(event.getUploadItem().getFile().getAbsolutePath());
		deployExpFile();
	}
	public void deployExpFile() {
		try {
			EObject eObject = dslUtil.convertExpTextToEcore(this.getMyexpPath());
			if(eObject != null) {
				System.out.println(this.rootModel);
				System.out.println(eObject);
				this.rootModel = eObject;
				this.rootNode = new TreeNodeImpl<EObject>();
				
				updateTreeNode(null);
				this.selectedNode = this.rootNode.getChild(this.getRootModel());
				this.myexpPath = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.myexpPath = null;
			e.printStackTrace();
			addError(e.getMessage());
		}
	}
	
	@Override
	protected Workflow getExampleForFind() {
		return new Workflow();
	}

	public EXPDSLUtil getDslUtil() {
		return dslUtil;
	}

	public void setDslUtil(EXPDSLUtil dslUtil) {
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

	public EReferenceImpl getSelectedRef() {
		return selectedRef;
	}

	public void setSelectedRef(EReferenceImpl selectedRef) {
		this.selectedRef = selectedRef;
	}

	public String getMyexpPath() {
		return myexpPath;
	}

	public void setMyexpPath(String myexpPath) {
		this.myexpPath = myexpPath;
	}

	public void clearExpFilePath() {
		this.setMyexpPath(null);
	}
}
