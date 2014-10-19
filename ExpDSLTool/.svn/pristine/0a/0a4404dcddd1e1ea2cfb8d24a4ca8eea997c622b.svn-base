package br.ufrn.dimap.dsl.expdsl.tool;
import java.util.Collections;
import java.util.List;

import jpdl31.Jpdl31Factory;
import jpdl31.Jpdl31Package;
import jpdl31.impl.Jpdl31FactoryImpl;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.m2m.qvt.oml.BasicModelExtent;
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl;
import org.eclipse.m2m.qvt.oml.ExecutionDiagnostic;
import org.eclipse.m2m.qvt.oml.ModelExtent;
import org.eclipse.m2m.qvt.oml.TransformationExecutor;

import br.ufrn.dimap.ase.dsl.expDslv2.ExpDslv2Factory;
import br.ufrn.dimap.ase.dsl.expDslv2.ExpDslv2Package;

public class QVTOTransformationExecutor {
	private String qvtoFilePath;
	
	public QVTOTransformationExecutor(String qvtoFilePath) {
		this.qvtoFilePath = qvtoFilePath;
	}
	
	public void execute(String modelPath, String modelOutPath) throws Throwable {
		//register the dsl package
		ExpDslv2Package.eINSTANCE.eClass(); //to register the package
		Jpdl31Package.eINSTANCE.eClass();
		// Retrieve the default factory
	    ExpDslv2Factory.eINSTANCE.eClass();
	    Jpdl31Factory.eINSTANCE.eClass();
	    
		//configure the resource factory
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());	
		
		//link the model to be transformed
		//get the list of EObject in this model and create the ModelExtend
		Resource inResource = resourceSet.getResource(URI.createFileURI(modelPath), true);		
		EList<EObject> inObjects = inResource.getContents();		
		ModelExtent input = new BasicModelExtent(inObjects);		
		
		//create the container to hold the output
		ModelExtent output = new BasicModelExtent();
		
		//create the context
		ExecutionContextImpl context = new ExecutionContextImpl();
		context.setConfigProperty("keepModeling", true);
		
		//link to qtvo file transformation
		//execute the transformation
		URI transformationURI = URI.createFileURI(qvtoFilePath);
		TransformationExecutor executor = new TransformationExecutor(transformationURI);
		ExecutionDiagnostic result = executor.execute(context, input, output);
		
		//check success
		if(result.getSeverity() == Diagnostic.OK) {
			// the output objects got captured in the output extent
			List<EObject> outObjects = output.getContents();
			// let's persist them using a resource 
	        ResourceSet resourceSet2 = new ResourceSetImpl();
	        resourceSet2.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
			Resource outResource = resourceSet2.createResource(URI.createFileURI(modelOutPath));
			outResource.getContents().addAll(outObjects);
			outResource.save(Collections.emptyMap());
		} else {
			System.err.println("NON OK");
			throw result.getException();
		}
	}
}
