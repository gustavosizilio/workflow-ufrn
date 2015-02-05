package org.domain.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.domain.utils.BasicPasswordEncryptor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("user")
@Scope(ScopeType.SESSION)
@Entity
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String email;
	private String encryptedPassword;
	private String name;

	public User() {
	}
	public User(String email, String encryptedPassword, String name) {
		this.setEmail(email);
		this.setEncryptedPassword(encryptedPassword);
		this.setName(name);
	}
	public String toString() {
		if(this.name != null && !this.name.isEmpty()) {
			return this.name;
		} else {
			return this.email;
		}
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getEncryptedPassword() {
		return encryptedPassword;
	}
	public void setPassword(String password) {
		this.encryptedPassword = new BasicPasswordEncryptor().encryptPassword(password);
	}
	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean equals(Object object){
		if(object instanceof User){
			if(((User)object).getId().equals(this.getId())){
				return true;
			}
		}
		return false;
	}
	public int hashCode(){
		return getId().hashCode();
	}
	 
}
