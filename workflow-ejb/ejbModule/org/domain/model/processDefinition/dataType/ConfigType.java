/* UNUSED

package org.domain.model.processDefinition.dataType;


public enum ConfigType {
	FIELD("field"),
	BEAN("bean"),
	CONSTRUCTOR("constructor"),
	CONFIGURATION_PROPERTY("configuration_property");
	
	private String name;
	private ConfigType(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public static ConfigType getValue(String value, ConfigType defaultValue){
		if(value == null){
			return defaultValue;
		}
		
		if(value.toLowerCase().trim().equals(ConfigType.BEAN.toString())){
			return ConfigType.BEAN;
		} else if(value.toLowerCase().trim().equals(ConfigType.FIELD.toString())){
			return ConfigType.FIELD;
		} else if(value.toLowerCase().trim().equals(ConfigType.CONSTRUCTOR.toString())){
			return ConfigType.CONSTRUCTOR;
		} else if(value.toLowerCase().trim().equals(ConfigType.CONFIGURATION_PROPERTY.toString())){
			return ConfigType.CONFIGURATION_PROPERTY;
		} else {
			return defaultValue;
		}
	}
}
*/