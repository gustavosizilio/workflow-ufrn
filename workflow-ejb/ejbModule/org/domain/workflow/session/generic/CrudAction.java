package org.domain.workflow.session.generic;

import java.util.List;

import org.domain.dao.SeamDAO;
import org.domain.model.User;
import org.domain.model.generic.CrudEntity;
import org.domain.model.generic.CrudType;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;


@Scope(ScopeType.CONVERSATION)
public abstract class CrudAction<T extends CrudEntity> {
	@In
	private FacesMessages facesMessages;
	protected final Class<T> type;
	protected CrudType crudType;
	protected List<T> entities;
	protected T entity;
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	
	public CrudAction(Class<T> type) {
		this.type = type;
	}
	
	/* BEGIN ACTIONS */
	protected void listImpl(){};
	
	@Begin(join=true, flushMode = FlushModeType.MANUAL)
	public String list(){
		crudType = CrudType.LIST;
		seamDao.clear();
		findEntities();
		listImpl();
		return getPage();
	}

	
	protected void detailImpl(){};
	@Begin(join=true, flushMode = FlushModeType.MANUAL)
	public String detail(T entity){
		crudType = CrudType.DETAIL;
		this.entity = entity;
		detailImpl();
		return getPage();
	}
	
	protected void createImpl(){};
	@Begin(join=true, flushMode = FlushModeType.MANUAL)
	@SuppressWarnings("unchecked")
	public String create() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		crudType = CrudType.CREATE;
		Class<T> classDefinition;
		classDefinition = (Class<T>) Class.forName(type.getCanonicalName());
		this.entity = classDefinition.newInstance();
		createImpl();
		return getPage();
	}
	
	protected void editImpl(){};
	@Begin(join=true, flushMode = FlushModeType.MANUAL)
	public String edit(T entity){
		crudType = CrudType.EDIT;
		this.entity = entity;
		editImpl();
		return getPage();
	}
	
	protected boolean saveImpl(){
		if(this.entity.isValid()){
			if(this.crudType.equals(CrudType.CREATE)){
				seamDao.persist(this.entity);
			}
			if(this.crudType.equals(CrudType.EDIT)){
				seamDao.merge(this.entity);
			}
			seamDao.flush();
			addInfo("Atualização efetuada com sucesso.");
			return true;
		} else {
			addErrors(this.entity.getErrors());
			return false;
		}
	};
	
	public String save(){
		if (saveImpl()){
			return list();
		} else {
			return getPage();
		}
	}
	
	protected boolean deleteImpl(){
		if(this.entity.isDeletable()){
			seamDao.remove(this.entity);
			addInfo("Remoção efetuada com sucesso.");
			seamDao.flush();
			return true;
		} else {
			addErrors(this.entity.getErrors());
			return false;
		}
	};
	
	public String delete(T entity){
		crudType = CrudType.DELETE;
		this.entity = entity;
		deleteImpl();
		return list();
	}
	
	private String getPage() {
		return "/" + getType().toLowerCase() + "/" + crudType.toString().toLowerCase() + ".xhtml";
	}
	/* END ACTIONS */
	
	public void findEntities()
	{
		setEntities(this.seamDao.findByExample(getExampleForFind()));
	}
	
	protected abstract T getExampleForFind();

	public String getType() {
		return type.getSimpleName();
	}
	
	public List<T> getEntities() {
		return entities;
	}

	public void setEntities(List<T> entities) {
		this.entities = entities;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}
	
	public FacesMessages getFacesMessages() {
		if (facesMessages == null) {
			facesMessages = (FacesMessages) Component.getInstance(FacesMessages.class);
		}
		return facesMessages;
	}
	
	public void addInfo(String message) {
		getFacesMessages().add(Severity.INFO, message);
	}
	
	public void addError(String message) {
		getFacesMessages().add(Severity.ERROR, message);
	}
	
	private void addErrors(List<String> errors) {
		for (String string : errors) {
			addError(string);
		}
	}
	
	public void setFacesMessages(FacesMessages facesMessages) {
		this.facesMessages = facesMessages;
	}
	
}
