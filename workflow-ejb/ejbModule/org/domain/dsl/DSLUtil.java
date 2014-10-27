package org.domain.dsl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.XtextResourceSet;

import br.ufrn.dimap.ase.dsl.Expdslv3RuntimeModule;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class DSLUtil {
	private static DSLUtil instance;
	private EFactoryImpl factory;
	private static final String packageClass = "br.ufrn.dimap.ase.dsl.expdslv3.Expdslv3Package";
	
	public synchronized static DSLUtil getInstance() throws Exception {
		if(instance == null) {
			instance = new DSLUtil();
		}
		return instance;
	}
	
	private DSLUtil() throws Exception {
		String factoryClass = packageClass.substring(0, packageClass.lastIndexOf("Package"))+"Factory";
		Class<?> packageFactory;
		Class<?> clazzFactory;
		
		packageFactory = Class.forName(packageClass);
		packageFactory.getField("eINSTANCE"); //registra a package
		
		clazzFactory = Class.forName(factoryClass);
		Field f = clazzFactory.getField("eINSTANCE");
		factory = (EFactoryImpl) f.get(null); //recupera a factory
	}
	
	public EObject getRootElement() {
		//cria a raiz
		EClassImpl eclazz = (EClassImpl) factory.getEPackage().eContents().get(0);
		EObject raiz = factory.create(eclazz);
		return raiz;
	}
	
	public List<EObject>  getAttrs(EObject raiz){
		List<EObject> attrs = new ArrayList<EObject>();
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
		return attrs;
	}
	
	public List<EReference>  getRefs(EObject raiz){
		List<EReference> refs = new ArrayList<EReference>();
		EClassImpl eclazz = (EClassImpl) raiz.eClass();
		for (EObject eObject : eclazz.eContents()) {
			if(eObject instanceof EReference) {
				if(((EReferenceImpl)eObject).isContainment()) {
					refs.add((EReferenceImpl)eObject);
				}
			}
		}
		return refs;
	} 
	
	public Object  getValue(EObject raiz, EObject attr) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String methodGetName = null;
		if(attr instanceof EAttribute) {
			methodGetName = "get"+Character.toUpperCase(((EAttribute)attr).getName().charAt(0)) + ((EAttribute)attr).getName().substring(1);
		} else if (attr instanceof EReference) {
			methodGetName = "get"+Character.toUpperCase(((EReference)attr).getName().charAt(0)) + ((EReference)attr).getName().substring(1);
		}
		if(methodGetName != null) {
			Method mGet = raiz.getClass().getDeclaredMethod(methodGetName);
			return mGet.invoke(raiz);
		} else {
			return null;
		}
	} 
	
	public void  buildRef(EObject raiz, EReference ref, EClass refClass) throws Exception {
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

	public byte[] convertEcoreToExpText(EObject model) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Injector injector = createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		URI uri = URI.createURI("tmp."+factory.getEPackage().getNsPrefix());
		Resource xtextResource = resourceSet.createResource(uri);
		xtextResource.getContents().add(model);
		EcoreUtil.resolveAll(xtextResource);
		//xtextResource.save(null);
		xtextResource.save(bos, null);
		return bos.toByteArray();
	}
	
	public Injector createInjectorAndDoEMFRegistration() {
		org.eclipse.xtext.common.TerminalsStandaloneSetup.doSetup();
		Injector injector = createInjector();
		register(injector);
		return injector;
	}
	
	public void register(Injector injector) {
		if (!EPackage.Registry.INSTANCE.containsKey(factory.getEPackage().getNsURI())) {
			EPackage.Registry.INSTANCE.put(factory.getEPackage().getNsURI(), br.ufrn.dimap.ase.dsl.expdslv3.Expdslv3Package.eINSTANCE);
		}

		org.eclipse.xtext.resource.IResourceFactory resourceFactory = injector.getInstance(org.eclipse.xtext.resource.IResourceFactory.class);
		org.eclipse.xtext.resource.IResourceServiceProvider serviceProvider = injector.getInstance(org.eclipse.xtext.resource.IResourceServiceProvider.class);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(factory.getEPackage().getNsPrefix(), resourceFactory);
		org.eclipse.xtext.resource.IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put(factory.getEPackage().getNsPrefix(), serviceProvider);
	}
	
	public Injector createInjector() {
		return Guice.createInjector(new Expdslv3RuntimeModule());
	}
	
}
