package org.domain.model.processDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Plan {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="plan")
	private List<DepVariable> depVariables;
	
	public Plan() {
		setDepVariables(new ArrayList<DepVariable>());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<DepVariable> getDepVariables() {
		return depVariables;
	}

	public void setDepVariables(List<DepVariable> depVariables) {
		this.depVariables = depVariables;
	}

}
