package org.domain.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.domain.model.generic.GenericEntity;
import org.domain.model.processDefinition.ProcessDefinition;

@Entity
public class Workflow extends GenericEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private User user;
	
	private String title;
	
	private String description;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<ProcessDefinition> processDefinitions;
	
	public Workflow() {
		this.setProcessDefinitions(new ArrayList<ProcessDefinition>());
	}
	public Workflow(User user) {
		this();
		this.user = user;
	}
	public Workflow(User user, String title) {
		this(user);
		this.title = title;
	}

	@Override
	public void validate() {
		if(title == null || title.trim().length() == 0){
			addError("Campo Título é obrigatório");
		}
		for (ProcessDefinition process : processDefinitions) {
			if(!process.isValid()){
				addErrors(process.getErrors());
			}
		}
	}
	@Override
	public void validateDeletable() {
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<ProcessDefinition> getProcessDefinitions() {
		return processDefinitions;
	}
	public void setProcessDefinitions(List<ProcessDefinition> processDefinitions) {
		this.processDefinitions = processDefinitions;
	}

}
