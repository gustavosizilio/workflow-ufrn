package org.domain.dao;

import java.util.List;

import javax.persistence.Query;

import org.domain.model.User;
import org.domain.model.processDefinition.UserAssignment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("userAssignmentDao")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class UserAssignmentDAO extends SeamDAO {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public List<UserAssignment> findAllOpened(User user){
		Query q = createQuery("SELECT u FROM UserAssignment u WHERE u.user = :user");
		q.setParameter("user", user);
		return (List<UserAssignment>) q.getResultList();
	}
}
