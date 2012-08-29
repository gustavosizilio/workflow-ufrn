package org.domain.workflow.session;

import org.domain.dao.SeamDAO;
import org.domain.model.User;
import org.domain.model.Workflow;
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
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
	public String init(Workflow workflow){
		this.setWorkflow(workflow);
		return WORKFLOW_REPORT_XHTML;
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
}
