package org.domain.utils;

import org.domain.dsl.EXPDSLUtil;
import org.domain.dsl.JPDLDSLUtil;
import org.domain.model.processDefinition.Workflow;

public class PathBuilder {
	public static synchronized String getExperimentsPath(){
		String expHome = System.getenv("EXPERIMENTS_HOME");
		if(expHome==null || expHome.isEmpty()){
			System.err.println("expHome: " + expHome);
			return validate("/tmp/experiments/");
		} else {
			return validate(expHome);			
		}
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
