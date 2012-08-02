package org.domain.dao;

import java.util.List;

import javax.persistence.Query;

import org.domain.model.User;
import org.domain.model.processDefinition.ProcessDefinition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("processDao")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcessDefinitionDAO extends SeamDAO {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public List<ProcessDefinition> findAllOpened(User user){
		Query q = createQuery("SELECT p FROM ProcessDefinition p WHERE p.startedAt IS NOT NULL AND :user MEMBER OF p.users");
		q.setParameter("user", user);
		return (List<ProcessDefinition>) q.getResultList();
	}
}
