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
public class Controller {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	/*private String clazz;
	@Enumerated(EnumType.STRING)
	private ConfigType configType;*/
	@OneToMany(cascade=CascadeType.ALL)
	private List<Variable> variables;
	
	public Controller() {
		variables = new ArrayList<Variable>();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	/*public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public ConfigType getConfigType() {
		return configType;
	}
	public void setConfigType(ConfigType configType) {
		this.configType = configType;
	}*/
	public List<Variable> getVariables() {
		return variables;
	}
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
	public String toString(){
		String variablesString = "";
		for (Variable variable : variables) {
			variablesString += variable;
		}
		/*return "{class='"+this.clazz+"', config-type='"+this.configType+"'" +
				", variables=["+variablesString+"]}";*/
		return "{variables=["+variablesString+"]}";
	}
}
