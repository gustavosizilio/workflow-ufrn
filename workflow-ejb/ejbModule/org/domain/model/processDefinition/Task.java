package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.domain.model.processDefinition.dataType.ArtefactType;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="task")
	private List<Artefact> artefacts;
	@ManyToOne
	private TaskNode taskNode;
	
	
	public Task() {
		this.artefacts = new ArrayList<Artefact>();
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<Artefact> getOutArtefacts() {
		List<Artefact> outArtefacts = new ArrayList<Artefact>();
		for (Artefact artefact : artefacts) {
			if(artefact.getArtefactType().equals(ArtefactType.OUT)){
				outArtefacts.add(artefact);
			}
		}
		return outArtefacts;
	}
	public List<Artefact> getInArtefacts() {
		List<Artefact> inArtefacts = new ArrayList<Artefact>();
		for (Artefact artefact : artefacts) {
			if(artefact.getArtefactType().equals(ArtefactType.IN)){
				inArtefacts.add(artefact);
			}
		}
		return inArtefacts;
	}
	
	public List<Artefact> getArtefacts() {
		return artefacts;
	}
	public void setArtefacts(List<Artefact> artefacts) {
		this.artefacts = artefacts;
	}

	public TaskNode getTaskNode() {
		return taskNode;
	}

	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}
}
