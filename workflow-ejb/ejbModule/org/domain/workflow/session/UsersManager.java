package org.domain.workflow.session;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.domain.dao.SeamDAO;
import org.domain.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

@Name("usersManager")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class UsersManager {
	
	private static final String USERS_XHTML = "/workflow/users.xhtml";
	private List<User> users = new ArrayList<User>();
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	private String newPass;
	@In private FacesMessages facesMessages;
	
	@Begin(flushMode=FlushModeType.MANUAL, join=true)
	public String init(){
		this.users = seamDao.findAll(User.class);
		return USERS_XHTML;
	}

	public void resetPass(User userupdate) {
		String passwordString = UUID.randomUUID().toString();
		passwordString = passwordString.split("-")[0];
		userupdate.setPassword(passwordString);
		seamDao.merge(userupdate);
		seamDao.flush();
		
		facesMessages.add(Severity.INFO,"The user "+userupdate.getEmail()+" was updated with a new password: "+passwordString);
	}
	
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}
	
}
