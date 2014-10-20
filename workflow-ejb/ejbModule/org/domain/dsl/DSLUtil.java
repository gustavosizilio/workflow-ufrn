package org.domain.dsl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;


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
	
	public List<EAttribute>  getAttrs(EObject raiz){
		List<EAttribute> attrs = new ArrayList<EAttribute>();
		EClassImpl eclazz = (EClassImpl) raiz.eClass();
		for (EObject eObject : eclazz.eContents()) {
			if(eObject instanceof EAttribute) {
				attrs.add((EAttribute) eObject);
			}
		}
		return attrs;
	}
	
	public List<EReference>  getRefs(EObject raiz){
		List<EReference> refs = new ArrayList<EReference>();
		EClassImpl eclazz = (EClassImpl) raiz.eClass();
		for (EObject eObject : eclazz.eContents()) {
			if(eObject instanceof EReference) {
				refs.add((EReferenceImpl)eObject);
			}
		}
		return refs;
	} 
	
	public Object  getValue(EObject raiz, EAttribute attr) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String methodGetName = "get"+Character.toUpperCase(attr.getName().charAt(0)) + attr.getName().substring(1);
		Method mGet = raiz.getClass().getDeclaredMethod(methodGetName);
		return mGet.invoke(raiz);
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
}
