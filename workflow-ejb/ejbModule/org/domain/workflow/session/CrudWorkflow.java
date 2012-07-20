package org.domain.workflow.session;

import org.domain.model.Workflow;
import org.domain.workflow.session.generic.CrudAction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

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
}
