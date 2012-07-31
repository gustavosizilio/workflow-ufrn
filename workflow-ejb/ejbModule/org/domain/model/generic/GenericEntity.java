package org.domain.model.generic;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class GenericEntity {
	@Transient
	private List<String> errors;
	
	public GenericEntity() {
		this.errors = new ArrayList<String>();
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	protected void addError(String error) {
		this.errors.add(error);
	}
	protected void addErrors(List<String> errors) {
		for (String error : errors) {
			addError(error);
		}
	}
	
	public abstract void validate();
	
	public boolean isValid(){
		this.errors.clear();
		this.validate();
		return this.errors.isEmpty();
	}
	
	public abstract void validateDeletable();
	
	public boolean isDeletable(){
		this.errors.clear();
		this.validateDeletable();
		return this.errors.isEmpty();
	}

}
