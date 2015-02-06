package org.domain.model.processDefinition;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ArtefactFile {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(cascade={CascadeType.REFRESH})
	private Artefact artefact;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private UserExecution userExecution;
	private String file;
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	public Artefact getArtefact() {
		return artefact;
	}
	public void setArtefact(Artefact artefact) {
		this.artefact = artefact;
	}
	public UserExecution getUserExecution() {
		return userExecution;
	}
	public void setUserExecution(UserExecution userExecution) {
		this.userExecution = userExecution;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
		
}
