package org.domain.dsl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.domain.model.processDefinition.Workflow;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import br.ufrn.dimap.ase.dsl.Expdslv3RuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class EXPDSLUtil {
	private static EXPDSLUtil instance;
	private EFactoryImpl factory;
	private Injector injector;
	private static final String packageClass = "br.ufrn.dimap.ase.dsl.expdslv3.Expdslv3Package";
	
	public synchronized static EXPDSLUtil getInstance() throws Exception {
		if(instance == null) {
			instance = new EXPDSLUtil();
		}
		return instance;
	}
	
	private EXPDSLUtil() throws Exception {
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
		
		createInjectorAndDoEMFRegistration();
	}
	
	public EObject getRootElement() {
		//cria a raiz
		EClassImpl eclazz = (EClassImpl) factory.getEPackage().eContents().get(0);
		EObject raiz = factory.create(eclazz);
		return raiz;
	}
	
	public List<EObject>  getAttrs(EObject raiz){
		List<EObject> attrs = new ArrayList<EObject>();
		if(raiz != null) {
			EClassImpl eclazz = (EClassImpl) raiz.eClass();
			for (EObject eObject : eclazz.eContents()) {
				if(eObject instanceof EAttribute) {
					attrs.add((EAttribute) eObject);
				} else if(eObject instanceof EReference) {
					if(!((EReferenceImpl)eObject).isContainment()) {
						attrs.add((EReferenceImpl)eObject);
					}
				}
			}
		}
		return attrs;
	}
	
	@SuppressWarnings("unchecked")
	public List<EObject> getRefsObjects(EObject raiz, EReference ref) {
		List<EObject> refObjects = new ArrayList<EObject>();
		if(raiz != null) {
			Object o = raiz.eGet(ref);
			if(o instanceof List<?>) {
				refObjects.addAll((List<EObject>) o);
			} else if (o instanceof EObject) {
				refObjects.add((EObject) o);
			}
		}
		return refObjects;
	}
	
	public List<EReference>  getRefs(EObject raiz){
		List<EReference> refs = new ArrayList<EReference>();
		if(raiz != null) {
			EClassImpl eclazz = (EClassImpl) raiz.eClass();
			for (EObject eObject : eclazz.eContents()) {
				if(eObject instanceof EReference) {
					if(((EReferenceImpl)eObject).isContainment()) {
						refs.add((EReferenceImpl)eObject);
					}
				}
			}
		}
		return refs;
	} 
	
	public Object  getValue(EObject raiz, EStructuralFeature attr) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		if(raiz != null)
			return raiz.eGet((EStructuralFeature)attr);
		else
			return null;
	} 
	
	public void setValue(EObject data, EStructuralFeature refAttr, Object value) throws Exception {
		data.eSet(refAttr, value);
	}
	
	@SuppressWarnings("unchecked")
	public void removeRef(EObject raiz, EObject obj, EReferenceImpl ref) throws Exception {
		//GET reference and add
		String refProperty = ref.getName();
		String methodGetName = "get"+Character.toUpperCase(refProperty.charAt(0)) + refProperty.substring(1);
		String methodSetName = "set"+Character.toUpperCase(refProperty.charAt(0)) + refProperty.substring(1);
		
		if(ref.isMany()) {
			Method mGet = raiz.getClass().getDeclaredMethod(methodGetName);
			EList<EObject> property = (EList<EObject>) mGet.invoke(raiz);
			property.remove(obj);
		} else {
			Method mSet = raiz.getClass().getDeclaredMethod(methodSetName, ref.getEReferenceType().getInstanceClass());
			mSet.invoke(raiz, (Object) null);
		}
	}	
	
	
	public EObject  buildRef(EObject raiz, EReference ref, EClass refClass) throws Exception {
		EClassImpl eclazz = (EClassImpl) raiz.eClass();
		EObject builtRef = factory.create(refClass);
		
		//GET reference and add
		String refProperty = ref.getName();
		String methodGetName = "get"+Character.toUpperCase(refProperty.charAt(0)) + refProperty.substring(1);
		String methodSetName = "set"+Character.toUpperCase(refProperty.charAt(0)) + refProperty.substring(1);
		
		if(ref.isMany()) {
			Method mGet = raiz.getClass().getDeclaredMethod(methodGetName);
			EList<EObject> property = (EList<EObject>) mGet.invoke(raiz);
			property.add(builtRef);
		} else {
			Method mSet = raiz.getClass().getDeclaredMethod(methodSetName, ref.getEReferenceType().getInstanceClass());
			mSet.invoke(raiz, builtRef);
		}
		return builtRef;
	}
	
	public List<EObject> getRefValues(EObject rootModel2, EObject eObject) {
		List<EObject> l = new ArrayList<EObject>();
		if(eObject instanceof EReference) {
			eObject = (EReference) eObject;
			for (EObject o : rootModel2.eContents()) {
				if(((EReference) eObject).getEReferenceType().getInstanceClass().isAssignableFrom(o.getClass())) {
					if(!eObject.equals(o)){
						l.add(o);
					}
				}
				l.addAll(getRefValues(o, eObject));
			}
		}
		return l;
	}
	
	public List<EClass> findClasses(EReference ref) {
		List<EClass> derivedClasses = new ArrayList<EClass>();
		derivedClasses.add(ref.getEReferenceType());
		EList<EObject> contents = factory.getEPackage().eContents();
		for (EObject eObject : contents) {
			if(eObject instanceof EClass) {
				EList<EClass> superTypes = ((EClass) eObject).getESuperTypes();
				if(superTypes.contains(ref.getEReferenceType())) {
					derivedClasses.add((EClass) eObject);
				}
			}
		}
		return derivedClasses;
	}

	public static void main(String[] args) {
		try {
			EXPDSLUtil util = EXPDSLUtil.getInstance();
			util.convertExpTextToEcore("/Users/buda/workspace/workspace_experimento/workflow-ufrn/ArtefatosMyExp/comprehensionLPSTest.expdslv3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public EObject convertExpTextToEcore(String path) throws IOException {
		String newPath = path.substring(0, path.lastIndexOf(".")+1)+factory.getEPackage().getNsPrefix();
		File afile = new File(path);
		afile.renameTo(new File(newPath));
		
		Injector injector = createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		
		Resource resource = resourceSet.getResource(URI.createURI(newPath), true);
		return resource.getContents().get(0);
		
	}
	
	public void convertEcoreToExpText(EObject model, String path) throws IOException {
		Injector injector = createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		//create the whole dir
		File file = new File(path);
		file.getParentFile().mkdirs();
		URI uri = URI.createURI(path);
		Resource xtextResource = resourceSet.createResource(uri);
		xtextResource.getContents().add(model);
		EcoreUtil.resolveAll(xtextResource);
		xtextResource.save(null);
	}
	
	public void convertEcoreToXMI(EObject model, String path) throws IOException {
		Injector injector = createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		File file = new File(path);
		file.getParentFile().mkdirs();
		URI uri = URI.createURI(path);
		Resource xmiResource = resourceSet.createResource(uri);
		xmiResource.getContents().add(model);
		xmiResource.save(null);
	}
	
	public EObject convertXMIToEcore(String path) throws IOException {
		Injector injector = createInjectorAndDoEMFRegistration();
		
		XMIResource resource = new XMIResourceImpl(URI.createURI(path));
	    resource.load(null);
	    //System.out.println( resource.getContents().get(0) );
		//XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		//resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		
		//Resource resource = resourceSet.getResource(URI.createURI(path), true);
		//return resource.getContents().get(0);
	    
	    return resource.getContents().get(0);
	}
	
	public Injector createInjectorAndDoEMFRegistration() {
		if(injector == null) {
			org.eclipse.xtext.common.TerminalsStandaloneSetup.doSetup();
			injector = createInjector();
			register(injector);
		}
		return injector;
	}
	
	public void register(Injector injector) {
		if (!EPackage.Registry.INSTANCE.containsKey(factory.getEPackage().getNsURI())) {
			EPackage.Registry.INSTANCE.put(factory.getEPackage().getNsURI(), br.ufrn.dimap.ase.dsl.expdslv3.Expdslv3Package.eINSTANCE);
		}

		org.eclipse.xtext.resource.IResourceFactory resourceFactory = injector.getInstance(org.eclipse.xtext.resource.IResourceFactory.class);
		org.eclipse.xtext.resource.IResourceServiceProvider serviceProvider = injector.getInstance(org.eclipse.xtext.resource.IResourceServiceProvider.class);
		org.eclipse.xtext.resource.IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put(factory.getEPackage().getNsPrefix(), serviceProvider);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(factory.getEPackage().getNsPrefix(), resourceFactory);
	}
	
	public Injector createInjector() {
		return Guice.createInjector(new Expdslv3RuntimeModule());
	}

	public String getMyexpName(Workflow w) {
		return w.getId()+"."+factory.getEPackage().getNsPrefix();
	}
	
	public String getXMIName(Workflow w) {
		return w.getId()+".xmi";
	}

}
