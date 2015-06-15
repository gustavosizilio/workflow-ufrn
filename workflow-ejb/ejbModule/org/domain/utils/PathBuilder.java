package org.domain.utils;

import org.domain.dsl.EXPDSLUtil;
import org.domain.dsl.JPDLDSLUtil;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.Workflow;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("pathBuilder")
public class PathBuilder {
	
	private String expHome;
	private String jbossLibPath;
	private String webPath;
	private String mailSender;
	private String mailSenderPwd;
	
	@In(value = "expdslUtil", create = true) private EXPDSLUtil dslUtil;
	
	public String getExperimentsPath(){
		if(getExpHome()==null || getExpHome().isEmpty()){
			System.err.println("expHome: " + getExpHome());
			return validate("/tmp/experiments/");
		} else { 
			return validate(getExpHome());			
		}
	}
	
	public String getArtefactsPath(Workflow w, Artefact currentArtefact){
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
	
	public String getExperimentDataPath(Workflow w) {
		return validate(getExperimentPath(w) + "/data/");
	}

	public String getExperimentPath(Workflow w){
		return validate(getExperimentsPath()+"/"+w.getId()+"/");
	}
	
	public String getExperimentMyexpPath(Workflow w){
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+dslUtil.getMyexpName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentJpdlPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+JPDLDSLUtil.getInstance(jbossLibPath).getJpdlName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentConfPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+dslUtil.getConfName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentXMIPath(Workflow w){
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+dslUtil.getXMIName(w));
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentMetricsSheetPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+ "metrics.xls");
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentTaskResultsSheetPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+ "taskResults.xls");
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentQuestResultsSheetPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+ "questResults.xls");
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getExperimentTaskResultsSheetZipPath(Workflow w) {
		try {
			return validate(getExperimentsPath()+"/"+w.getId()+"/"+ "taskResults.zip");
		} catch (Exception e) {
			return null;
		}
	}
	
	private String validate(String path){
		return path.replaceAll("//", "/");
	}

	public String getExpHome() {
		return expHome;
	}

	public void setExpHome(String expHome) {
		this.expHome = expHome;
	}

	public String getWebPath() {
		return webPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}

	public String getJbossLibPath() {
		return jbossLibPath;
	}

	public void setJbossLibPath(String jbossLibPath) {
		this.jbossLibPath = jbossLibPath;
	}

	public String getMailSender() {
		return mailSender;
	}

	public void setMailSender(String mailSender) {
		this.mailSender = mailSender;
	}

	public String getMailSenderPwd() {
		return mailSenderPwd;
	}

	public void setMailSenderPwd(String mailSenderPwd) {
		this.mailSenderPwd = mailSenderPwd;
	}
	
}
