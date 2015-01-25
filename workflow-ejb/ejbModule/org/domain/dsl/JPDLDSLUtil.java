package org.domain.dsl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.domain.model.processDefinition.Workflow;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.jboss.seam.contexts.ServletLifecycle;

public class JPDLDSLUtil {
	private static JPDLDSLUtil instance;
	private EFactoryImpl factory;
	private String qvtoFilePath;
	private String transformationJar;
	private static final String packageClass = "jpdl31Plus.Jpdl31PlusPackage";
	
	public synchronized static JPDLDSLUtil getInstance() throws Exception {
		if(instance == null) {
			instance = new JPDLDSLUtil();
		}
		return instance;
	}
	
	private JPDLDSLUtil() throws Exception {
		qvtoFilePath = ServletLifecycle.getCurrentServletContext().getRealPath("/") + "../" + "QVTOTransformation.qvto";
		transformationJar = ServletLifecycle.getCurrentServletContext().getRealPath("/") + "../lib/ExpDSLTransformationsTool.jar";
		String factoryClass = packageClass.substring(0, packageClass.lastIndexOf("Package"))+"Factory";
		Class<?> packageFactory;
		Class<?> clazzFactory;
		
		
		packageFactory = Class.forName(packageClass);
		//packageFactory.getField("eINSTANCE");
		//packageFactory.getField("eINSTANCE").get(packageFactory); //registra a package
		
		clazzFactory = Class.forName(factoryClass);
		Field f = clazzFactory.getField("eINSTANCE");
		factory = (EFactoryImpl) f.get(null); //recupera a factory
		
		factory.getEPackage().eClass();
		factory.eClass();
		//EPackage.Registry.INSTANCE.put(factory.getEPackage().getNsURI(), factory.getEPackage());
	}
	
	public String getJpdlName(Workflow w) {
		return w.getId()+"."+factory.getEPackage().getNsPrefix();
	}

	public void convertXMIToJPDL(String xmiPath, String experimentJpdlPath) throws Exception {
		String cmd = "java -jar " + transformationJar + " \"qvto\"" + " \""+qvtoFilePath+"\"" + " \""+xmiPath+"\"" + " \""+experimentJpdlPath+"\"";
		Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
	    p.waitFor();
	 	BufferedReader reader = 
	         new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line = "";	
	    StringBuilder sb = new StringBuilder();
	    while ((line = reader.readLine())!= null) {
	    	sb.append(line + "\n");
	    }
	}

}
