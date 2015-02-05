package br.ufrn.dimap.dsl.expdsl.tool.transform;

import java.io.File;

import org.eclipse.emf.common.util.URI;

public class AcceleoTransformationExecuter {
	public void execute(String string, String string2) {
		// TODO Auto-generated method stub
		URI modelURI = URI.createFileURI("/Users/buda/workspace/workspace_experimento/workflow-ufrn/ExpDSLTransformationsTool/CKE2901.xmi");
		File targetFolder = new File("/tmp/");
		Generate generator = new Generate(modelURI, targetFolder, Collections.emptyList());
		generator.doGenerate();
	}
	
}
