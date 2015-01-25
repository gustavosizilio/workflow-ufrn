package br.ufrn.dimap.dsl.expdsl.tool;

import java.nio.file.Paths;

import br.ufrn.dimap.ase.dsl.ExpDslv2StandaloneSetup;


public class Executor {
	String expDSLModelPath;
	String xmiModelPath;
	String jpdlModelPath;
	String confModelPath;
	
	public static void main(String[] args) throws Throwable {
		Executor executor = new Executor();
		executor.execute("E:/Workspace/mestrado/marilia_project/workspaceExperimento/ExpDSLTool/test/", "ExemploProfes2014");
	}
	
	//workPath is the path where the expdsl must be located. All output files will be generated in the same path
	//modelName is the name of the expdsl file, without the extension. All output files will be generated using the same name but with diferent extension
	public void execute(String workPath, String modelName) throws Throwable{
		//change this to use class loader
		String qvtoFilePath = this.getClass().getClassLoader().getResource("ExpDSLv2ToWorkExpTese.qvto").getPath();
		//String qvtoFilePath = "E:/Workspace/mestrado/marilia_project/workspaceExperimento/testeQVTO/transforms/ExpDSLv2ToWorkExpTese.qvto";

		expDSLModelPath = Paths.get(workPath, modelName+".expdsl").toString();
		xmiModelPath = Paths.get(workPath, modelName+".xmi").toString();
		jpdlModelPath = Paths.get(workPath, modelName+".jpdl31").toString();
		confModelPath = Paths.get(workPath, modelName+".conf").toString();
		
		//First, create the xmi model from expdsl
		ExpDslv2StandaloneSetup expDslExecutor = new ExpDslv2StandaloneSetup();
		expDslExecutor.execute(expDSLModelPath, xmiModelPath);
		
		//Create the "jpdl" file from xmi model
		QVTOTransformationExecutor qvtoExecutor = new QVTOTransformationExecutor(qvtoFilePath); 
		qvtoExecutor.execute(xmiModelPath, jpdlModelPath);
		
		//Create the .conf file from xmi model
		AcceleoTransformationExecutor acceleoExecutor = new AcceleoTransformationExecutor();
		acceleoExecutor.execute(xmiModelPath, confModelPath);
	}

}
