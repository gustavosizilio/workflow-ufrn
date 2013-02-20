package org.domain.workflow.session;

import java.util.GregorianCalendar;

import org.domain.dao.SeamDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Observation;
import org.domain.model.processDefinition.Workflow;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

@Name("report")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowReport {
	private static final String WORKFLOW_REPORT_XHTML = "/workflow/report.xhtml";
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	private Workflow workflow;
	
	private Observation observation;
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
	public String init(Workflow workflow){
		this.setWorkflow(workflow);
		cleanObservation();		
		return WORKFLOW_REPORT_XHTML;
	}

	private void cleanObservation() {
		this.setObservation(new Observation(this.workflow));
	}
	
	public void wire(){
		seamDao.refresh(workflow);
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}
	
	public void saveObservation(){
		try {
			this.observation.setCreatedAt(new GregorianCalendar());
			seamDao.persist(this.observation);
			this.workflow.getObservations().add(this.observation);
			seamDao.merge(this.workflow);
			seamDao.flush();
			cleanObservation();
		} catch (ValidationException e) {
		}
	}
	public void removeObservation(Observation o){
		try {
			this.workflow.getObservations().remove(o);
			seamDao.remove(o);
			seamDao.merge(this.workflow);
			seamDao.flush();
			cleanObservation();
		} catch (ValidationException e) {
		}
	}
}
