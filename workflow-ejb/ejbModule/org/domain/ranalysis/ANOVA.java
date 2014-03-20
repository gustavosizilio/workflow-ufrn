package org.domain.ranalysis;
import java.io.File;
import java.io.IOException;

import rcaller.RCaller;
import rcaller.RCode;

public class ANOVA {

	public void test(){
		
			RCaller caller = new RCaller();
			caller.setRscriptExecutable("/opt/local/bin/Rscript");
			
			RCode code = new RCode();
			code.clear();
			
			code.addRCode("fertil <- c(rep(\"fertil1\",1), rep(\"fertil2\",1), rep(\"fertil3\",1), rep(\"fertil4\",1), rep(\"fertil5\",1))");
			code.addRCode("treat <- c(rep(\"treatA\",5), rep(\"treatB\",5), rep(\"treatC\",5), rep(\"treatD\",5), rep(\"treatE\",5))");
			code.addRCode("seed <- c(\"A\",\"E\",\"C\",\"B\",\"D\", \"C\",\"B\",\"A\",\"D\",\"E\", \"B\",\"C\",\"D\",\"E\",\"A\", \"D\",\"A\",\"E\",\"C\",\"B\", \"E\",\"D\",\"B\",\"A\",\"C\")");
			code.addRCode("freq <- c(42,45,41,56,47, 47,54,46,52,49, 55,52,57,49,45, 51,44,47,50,54, 44,50,48,43,46)");
			 
			code.addRCode("mydata <- data.frame(treat, fertil, seed, freq)");
			
			code.addRCode("myfit <- lm(freq ~ fertil+treat+seed, mydata)");
			
			
			
			caller.setRCode(code);
			caller.runAndReturnResult("anova(myfit)");
			
			try {
				System.out.println(caller.getParser().getXMLFileAsString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
	}
}
