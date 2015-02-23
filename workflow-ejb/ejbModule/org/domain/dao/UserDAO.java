package org.domain.dao;

import java.util.List;

import javax.persistence.Query;

import org.domain.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("userDao")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class UserDAO extends SeamDAO {
	private static final long serialVersionUID = 1L;
	
	public List<User> findAllByEmail(String email){
		return findByExample(new User(email,null,null));
	}
	
	@SuppressWarnings("unchecked")
	public List<User> findAllByName(String name){
		Query q = createQuery("SELECT u FROM User u where UPPER(u.name) LIKE '%' || UPPER(LTRIM(RTRIM(:userName))) || '%'");
		q.setParameter("userName", name);
		return (List<User>) q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<User> findAllByNameOrEmail(String search){	
		StringBuilder sb = new StringBuilder();
		String[] names = search.trim().split(" ");
		
		sb.append("SELECT u FROM User u where (true=true ");
		for (int i = 0; i < names.length; i++) {
			sb.append("AND UPPER(u.name) LIKE '%' || UPPER(LTRIM(RTRIM(?))) || '%'");
		}
		sb.append(") OR UPPER(u.email) LIKE '%' || UPPER(LTRIM(RTRIM(?))) || '%'");
		
		String query = sb.toString();
		Query q = createQuery(query);
		for (int i = 0; i < names.length; i++) {
			String string = names[i];
			q.setParameter(i+1, string);
		}
		q.setParameter(names.length+1, search);
		
		
		return (List<User>) q.getResultList();
	}
	
	public User findAuthenticate(String email, String encryptedPassword){
		List<User> users = findByExample(new User(email,encryptedPassword,null));
		if(users.size() == 1){
			return users.get(0);
		}else{
			return null;
		}	
	}
}
