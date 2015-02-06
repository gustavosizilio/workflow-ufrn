package org.domain.utils;

import org.domain.dsl.EXPDSLUtil;
import org.domain.dsl.JPDLDSLUtil;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.Workflow;

public class PathBuilder {
	public static synchronized String getExperimentsPath(){
		String expHome = ReadPropertiesFile.getProperty("components", "expHome");
		if(expHome==null || expHome.isEmpty()){
			System.err.println("expHome: " + expHome);
			return validate("/tmp/experiments/");
		} else {
			return validate(expHome);			
		}
	}
	
	public static synchronized String getArtefactsPath(Workflow w, Artefact currentArtefact){
		StringBuilder sb = new StringBuilder();
		sb.append(getExperimentDataPath(w));
		sb.append("/"+currentArtefact.getTaskNode().getProcessDefinition().getName()+"/");
		sb.append("/"+currentArtefact.getTaskNode().getName()+"/");
		if(currentArtefact.getTask() != null)
			sb.append("/"+currentArtefact.getTask().getName()+ "/"); 
		
		sb.append("/artefacts/");
		sb.append("/"+ currentArtefact.getArtefactType() +"/");
		sb.append("/"+currentArtefact.getName()+"/");
		
		return validate(sb.toString());
	}
	
	public static synchronized String getExperimentDataPath(Workflow w) {
		return validate(getExperimentPath(w) + "/data/");
	}

	public static synchronized String getExperimentPath(Workflow w){
		return validate(getExperimentsPath()+"/"+w.getId()+"/");
	}
	
	public static synchronized String getExperimentMyexpPath(Workflow w){
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+EXPDSLUtil.getInstance().getMyexpName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getExperimentJpdlPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+JPDLDSLUtil.getInstance().getJpdlName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getExperimentConfPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+EXPDSLUtil.getInstance().getConfName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static synchronized String getExperimentXMIPath(Workflow w){
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+EXPDSLUtil.getInstance().getXMIName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	private static String validate(String path){
		return path.replaceAll("//", "/");
	}
	
}
