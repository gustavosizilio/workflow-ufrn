package org.domain.model.processDefinition.dataType;

public enum ArtefactType {
		IN("in"),
		OUT("out");
		
		private String name;
		private ArtefactType(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
		public static ArtefactType getValue(String value, ArtefactType defaultValue){
			if(value == null){
				return defaultValue;
			}
			
			if(value.toLowerCase().trim().equals(ArtefactType.IN.getName())){
				return ArtefactType.IN;
			} else if(value.toLowerCase().trim().equals(ArtefactType.OUT.getName())){
				return ArtefactType.OUT;
			} else {
				return defaultValue;
			}
		}
}
