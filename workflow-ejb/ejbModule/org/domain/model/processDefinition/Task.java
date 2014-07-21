package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.domain.model.User;
import org.domain.model.processDefinition.dataType.ArtefactType;

@Entity
public class Task {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Lob
	private String description;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="task")
	private List<Artefact> artefacts;
	@ManyToOne
	private TaskNode taskNode;
	@OneToMany(mappedBy="task",cascade=CascadeType.ALL)
	private List<TaskExecution> taskExecutions;
	
	
	public Task() {
		this.artefacts = new ArrayList<Artefact>();
	}
	
	public boolean startedByUser(User user){
		if(getTaskExecutionByUser(user) != null){
			return true;
		} else {
			return false;
		}
	}
	public boolean finishedByUser(User user){
		TaskExecution taskExecution = getTaskExecutionByUser(user);
		if(taskExecution != null && taskExecution.getFinishedAt() != null){
			return true;
		} else {
			return false;
		}
	}
	public TaskExecution getTaskExecutionByUser(User user){
		for (TaskExecution taskExecution : taskExecutions) {
			if(taskExecution.getUser().equals(user)){
				return taskExecution;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public List<TaskExecution> getTaskExecutions() {
		return taskExecutions;
	}

	public void setTaskExecutions(List<TaskExecution> taskExecutions) {
		this.taskExecutions = taskExecutions;
	}
	
	
}
