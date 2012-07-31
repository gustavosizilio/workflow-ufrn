package org.domain.model.processDefinition;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Assignment {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String expression;
	private String actorId;
	private String pooledActors;
	
	public Assignment() {
	}
	public Assignment(String expression, String actorId, String pooledActors) {
		this();
		this.expression = expression;
		this.actorId = actorId;
		this.pooledActors = pooledActors;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getActorId() {
		return actorId;
	}

	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public String getPooledActors() {
		return pooledActors;
	}

	public void setPooledActors(String pooledActors) {
		this.pooledActors = pooledActors;
	}
}
