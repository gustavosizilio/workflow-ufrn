package org.domain.ranalysis;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.domain.model.processDefinition.Workflow;

import rcaller.RCaller;
import rcaller.RCode;

public class ANOVA {

	private static ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	private static final String DEST_PATH = "/img/generated_graph/";
	private static final String RSCRIPT = "/opt/local/bin/Rscript";
	private Workflow workflow;
	private String pathGraph = "";
	private HashMap<String, String[]> analysisSummary = new HashMap<String, String[]>();
	private List<String> factors = new ArrayList<String>();
	
	public void build(Workflow workflow){
			this.workflow = workflow;
			anovaAnalysis();
	}
	
	private void anovaAnalysis() {
		getFactors().add("System");getFactors().add("Subject");getFactors().add("ProgrammingLanguage");
		
		//ANOVA ANALISYS
		RCaller caller = new RCaller();
		caller.setRscriptExecutable(RSCRIPT);
		
		RCode code = new RCode();
		code.clear();
		
		code.addRCode("System <- c(rep(\"PhoneBook\",1), rep(\"EventManagement\",1))");//coluna
		code.addRCode("Subject <- c(rep(\"A\",4), rep(\"B\",4))");//linha -- 4 pois temos 2 replicas
		code.addRCode("ProgrammingLanguage <- c(\"Java\", \"CPlusPlus\","
								+"\"Java\", \"CPlusPlus\","
								+"\"CPlusPlus\", \"Java\","
								+"\"CPlusPlus\", \"Java\")");//tratamento
		

		code.addRCode("data <- c(171,176,"
								+"102.5,354,"
								+"313.2,145.6,"
								+"171.8,108.3)");
		 
		String strFactorsComa = "";
		for (String s : getFactors()) {
			strFactorsComa += s + ", ";
		}
		String strFactorsPlus = "";
		for (String s : getFactors()) {
			strFactorsPlus += s + "+";
		}
		strFactorsPlus = strFactorsPlus.substring(0, strFactorsPlus.length()-1);
		
		
		code.addRCode("mydata <- data.frame("+ strFactorsComa +" data)");
		code.addRCode("myfit <- lm(data ~ "+ strFactorsPlus +", mydata)");
		
		
		
		caller.setRCode(code);
		caller.runAndReturnResult("anova(myfit)");
		ArrayList<String> names = caller.getParser().getNames();
		
		for (String name : names) {
			getAnalysisSummary().put(name, caller.getParser().getAsStringArray(name));
		}
		
		//GRAPH
		RCaller caller2 = new RCaller();
		caller2.setRscriptExecutable(RSCRIPT);
		
		File file = null;
		try {
			file = code.startPlot();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		code.addRCode("par(mfrow=c(2,2))");
		code.addRCode("plot(data ~ "+strFactorsPlus+", mydata)");
		code.endPlot();

		caller2.setRCode(code);
		caller2.runOnly();
		code.showPlot(file);
		File fileSaved = new File(ctx.getRealPath(File.separator) + DEST_PATH+file.getName());
		file.renameTo(fileSaved);
		System.out.println("Plot was saved on : " + fileSaved);
		
		setPathGraph(DEST_PATH+file.getName());
		
	}

	public String getPathGraph() {
		return pathGraph;
	}

	public void setPathGraph(String pathGraph) {
		this.pathGraph = pathGraph;
	}

	public HashMap<String, String[]> getAnalysisSummary() {
		return analysisSummary;
	}

	public void setAnalysisSummary(HashMap<String, String[]> analysisSummary) {
		this.analysisSummary = analysisSummary;
	}

	public List<String> getFactors() {
		return factors;
	}

	public void setFactors(List<String> factors) {
		this.factors = factors;
	}

}
