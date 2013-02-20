package org.domain.model.processDefinition;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.domain.model.generic.GenericEntity;

@Entity
public class Observation extends GenericEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(cascade=CascadeType.REFRESH)
	private Workflow workflow;
	private String comment;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar createdAt;
	public Observation() {
	}
	public Observation(Workflow workflow) {
		this();
		this.workflow = workflow;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Workflow getWorkflow() {
		return workflow;
	}
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Calendar getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Calendar createdAt) {
		this.createdAt = createdAt;
	}
	@Override
	public void validate() {
		if(this.comment.isEmpty()){
			this.addError("É obrigatório preencher um o texto");
		}
		if(this.createdAt == null){
			this.addError("Data de criação não pode ser nula");
		}		
	}
	@Override
	public void validateDeletable() {
		
	}
	
	public boolean equals(Object o){
		if(o instanceof Observation){
			if(((Observation)o).getId().equals(this.getId())){
				return true;
			}
		}
		return false;
	}

}
