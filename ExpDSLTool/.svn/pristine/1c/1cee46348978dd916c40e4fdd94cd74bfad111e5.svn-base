package br.ufrn.dimap.dsl.expdsl.tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;

import br.ufrn.dimap.ase.dsl.expDslv2.impl.ExperimentElementImpl;

import AcceleoExpDSL.main.Generate;

public class AcceleoTransformationExecutor {
	
	public void execute(String modelPath, String confModelPath) throws IOException{
		URI modelURI = URI.createFileURI(modelPath);
		File targetFile = new File(confModelPath);
		Generate generator = new Generate(modelURI, targetFile.getParentFile(), new ArrayList<Object>());
		BasicMonitor monitor = new BasicMonitor();
		generator.doGenerate(monitor);
	
		String generatedFileName =((ExperimentElementImpl)generator.getModel().eContents().get(0)).getName().toLowerCase() + ".conf";
		File generatedFile = new File(targetFile.getParentFile(), generatedFileName);
		generatedFile.renameTo(targetFile);
	}
}
