package org.domain.model.processDefinition;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.domain.model.processDefinition.dataType.ArtefactType;

@Entity
public class Artefact {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private ArtefactType artefactType;
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
	public ArtefactType getArtefactType() {
		return artefactType;
	}
	public void setArtefactType(ArtefactType artefactType) {
		this.artefactType = artefactType;
	}
	
	@Override
	public String toString() {
		return "{name='"+this.name+"', type='"+this.getArtefactType().toString()+"'}";
	}
	
}
