package org.domain.model.processDefinition;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Variable {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String access;
	//private String mappedName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/*public String getMappedName() {
		return mappedName;
	}

	public void setMappedName(String mappedName) {
		this.mappedName = mappedName;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}
	public String toString(){
		/*return "{name='"+this.name+"', access='"+this.access+"', mapped-name='"+this.mappedName+"'}";*/
		return "{name='"+this.name+"', access='"+this.access+"'}";
	}
}
