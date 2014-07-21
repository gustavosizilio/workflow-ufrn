package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.domain.model.User;
import org.domain.model.processDefinition.dataType.ArtefactType;

@Entity
public class Artefact {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private ArtefactType artefactType;
	@ManyToOne
	private TaskNode taskNode;
	@ManyToOne
	private Task task;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="artefact")
	private List<ArtefactFile> artefactFiles;
	
	public Artefact() {
		this.artefactFiles = new ArrayList<ArtefactFile>();
	}
	
	public ArtefactFile getUserArtefactFile(User currentUser){
		for (ArtefactFile artefactFile : artefactFiles) {
			if(artefactFile.getUserExecution() != null && artefactFile.getUserExecution().getUser().equals(currentUser)){
				return artefactFile;
			}
		}
		return null;
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
	public ArtefactType getArtefactType() {
		return artefactType;
	}
	public void setArtefactType(ArtefactType artefactType) {
		this.artefactType = artefactType;
	}
	
	@Override
	public String toString() {
		return "{name='"+this.name+"', type='"+this.getArtefactType().getName()+"'}";
	}
	public TaskNode getTaskNode() {
		return taskNode;
	}
	public void setTaskNode(TaskNode taskNode) {
		this.taskNode = taskNode;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public List<ArtefactFile> getArtefactFiles() {
		return artefactFiles;
	}
	public void setArtefactFiles(List<ArtefactFile> artefactFiles) {
		this.artefactFiles = artefactFiles;
	}
	public ArtefactFile get(UserExecution currentUserExecution){
		for (ArtefactFile artefactFile : artefactFiles) {
			if(artefactFile.getUserExecution().equals(currentUserExecution)){
				return artefactFile;
			}
		}		
		return null;
	}
}
