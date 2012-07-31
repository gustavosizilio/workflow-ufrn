package org.domain.workflow.session;

import java.util.List;

import org.domain.exception.ValidationException;
import org.domain.model.Workflow;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.workflow.session.generic.CrudAction;
import org.domain.xml.JPDLManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("crudWorkflow")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class CrudWorkflow extends CrudAction<Workflow> {

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
}
