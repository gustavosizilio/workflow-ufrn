package org.domain.model.processDefinition.metric;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.domain.model.User;

@Entity
public class UserAnswer {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToOne(cascade=CascadeType.REFRESH)
	private User user;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar createdAt;
	private String answer;
	@ManyToOne
	private Question question;
	
	
	public Object getValue(){
		if(question.hasOptions()){
			return question.getOption(Long.parseLong(answer));
		}
		if(question.getType() == QuestionType.NUMERIC){
			return Long.parseLong(answer);
		}
		
		return null;
	}
}
