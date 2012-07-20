package org.domain.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.domain.model.generic.CrudEntity;

@Entity
public class Workflow extends CrudEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Lob
	private byte[] fileContent;
	
	@ManyToOne
	private User user;
	
	private String title;
	
	private String description;
	
	public Workflow() {
	}
	public Workflow(User user) {
		this();
		this.user = user;
	}

	@Override
	public void validate() {
		/*if(fileContent == null){
			addError("Um arquivo com a definição do processo é requerido");
		}*/
		if(title == null || title == ""){
			addError("Campo Título é obrigatório");
		}
	}
	@Override
	public void validateDeletable() {
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
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

}
