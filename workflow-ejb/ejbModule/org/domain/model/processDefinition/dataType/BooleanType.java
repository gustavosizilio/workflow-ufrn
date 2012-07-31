package org.domain.model.processDefinition.dataType;

public class BooleanType {
	public static Boolean getValue(String value, boolean defaultValue) {
		if(value == null){
			return defaultValue;
		}
		
		if(value.trim().toLowerCase().equals("yes") || value.trim().toLowerCase().equals("true")){
			return true;
		} else if (value.trim().toLowerCase().equals("no") || value.trim().toLowerCase().equals("false")){
			return false;
		} else {
			return defaultValue;
		}
	}
}
