package org.domain.dao;

import java.util.List;

import javax.persistence.Query;

import org.domain.model.User;
import org.domain.model.Workflow;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("workflowDAO")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class WorkflowDAO extends SeamDAO {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unchecked")
	public List<Workflow> findAllByUser(User user){
		Query q = createQuery("SELECT w FROM Workflow w where w.user = :user");
		q.setParameter("user", user);
		return (List<Workflow>) q.getResultList();
	}
}
