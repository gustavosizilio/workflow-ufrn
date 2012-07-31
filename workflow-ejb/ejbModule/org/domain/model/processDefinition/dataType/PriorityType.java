package org.domain.model.processDefinition.dataType;

public enum PriorityType {
	highest, high, normal, low, lowest;
	
	public static PriorityType getValue(String value, PriorityType defaultValue){
		if(value == null){
			return defaultValue;
		}
		
		if(value.toLowerCase().trim().equals(PriorityType.highest.toString())){
			return PriorityType.highest;
		} else if(value.toLowerCase().trim().equals(PriorityType.high.toString())){
			return PriorityType.high;
		} else if(value.toLowerCase().trim().equals(PriorityType.normal.toString())){
			return PriorityType.normal;
		} else if(value.toLowerCase().trim().equals(PriorityType.low.toString())){
			return PriorityType.low;
		} else if(value.toLowerCase().trim().equals(PriorityType.lowest.toString())){
			return PriorityType.lowest;
		} else {
			return defaultValue;
		}
	}
}
