package org.domain.workflow.session;

import org.domain.model.Workflow;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

@Name("executer")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowExecuter {

	private static final String WORKFLOW_EXECUTE_XHTML = "/workflow/execute.xhtml";

	@Begin(join=true, flushMode=FlushModeType.MANUAL)
	public String init(Workflow workflow){
		
		return WORKFLOW_EXECUTE_XHTML;
	}
}
