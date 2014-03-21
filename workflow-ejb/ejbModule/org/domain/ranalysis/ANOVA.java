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
		getFactors().add("fertil");getFactors().add("treat");getFactors().add("seed");
		
		//ANOVA ANALISYS
		RCaller caller = new RCaller();
		caller.setRscriptExecutable(RSCRIPT);
		
		RCode code = new RCode();
		code.clear();
		
		code.addRCode("fertil <- c(rep(\"fertil1\",1), rep(\"fertil2\",1), rep(\"fertil3\",1), rep(\"fertil4\",1), rep(\"fertil5\",1))");
		code.addRCode("treat <- c(rep(\"treatA\",5), rep(\"treatB\",5), rep(\"treatC\",5), rep(\"treatD\",5), rep(\"treatE\",5))");
		code.addRCode("seed <- c(\"A\",\"E\",\"C\",\"B\",\"D\", \"C\",\"B\",\"A\",\"D\",\"E\", \"B\",\"C\",\"D\",\"E\",\"A\", \"D\",\"A\",\"E\",\"C\",\"B\", \"E\",\"D\",\"B\",\"A\",\"C\")");
		code.addRCode("data <- c(42,45,41,56,47, 47,54,46,52,49, 55,52,57,49,45, 51,44,47,50,54, 44,50,48,43,46)");
		 
		String strFactors = "";
		for (String s : getFactors()) {
			strFactors += s + ", ";
		}
		code.addRCode("mydata <- data.frame("+ strFactors +" data)");
		
		code.addRCode("myfit <- lm(data ~ fertil+treat+seed, mydata)");
		
		
		
		caller.setRCode(code);
		caller.runAndReturnResult("anova(myfit)");
		ArrayList<String> names = caller.getParser().getNames();
		
		for (String name : names) {
			getAnalysisSummary().put(name, caller.getParser().getAsStringArray(name));
			try {
				System.out.println(caller.getParser().getXMLFileAsString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		code.addRCode("plot(data ~ fertil+treat+seed, mydata)");
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
