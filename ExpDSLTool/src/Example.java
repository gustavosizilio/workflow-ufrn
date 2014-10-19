
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
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

import br.ufrn.dimap.ase.dsl.expdslv3.DesignType;
import br.ufrn.dimap.ase.dsl.expdslv3.impl.KeywordImpl;
import br.ufrn.dimap.ase.dsl.expdslv3.impl.ModelImpl;
import br.ufrn.dimap.ase.dsl.expdslv3.impl.OTHERImpl;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class Example {
	static EFactoryImpl factory;
	
	public static void main(String[] args) {
		execute();
	}

	private static void execute() {
		String packageClass = "br.ufrn.dimap.ase.dsl.expdslv3.Expdslv3Package";
		String factoryClass = packageClass.substring(0, packageClass.lastIndexOf("Package"))+"Factory";
		Class<?> packageFactory;
		Class<?> clazzFactory;
		try {
			packageFactory = Class.forName(packageClass);
			packageFactory.getField("eINSTANCE"); //registra a package
			
			
			clazzFactory = Class.forName(factoryClass);
			Field f = clazzFactory.getField("eINSTANCE");
			factory = (EFactoryImpl) f.get(null); //recupera a factory
			
			//cria a raiz
			EClassImpl eclazz = (EClassImpl) factory.getEPackage().eContents().get(0);
			EObject raiz = factory.create(eclazz);
			buildRefs(raiz, 2);
			
			((ModelImpl)raiz).getElements().get(0).setName("EXP_TESTE");
			((ModelImpl)raiz).getElements().get(0).setDescription("Um experimento muito louco");
			((ModelImpl)raiz).getElements().get(0).getExperiments().setType(DesignType.OTHER);
			buildRef(((ModelImpl)raiz).getElements().get(0).getExperiments(), "doe", "OTHER");
			((OTHERImpl) ((ModelImpl)raiz).getElements().get(0).getExperiments().getDoe()).setName("oooo");
			((ModelImpl)raiz).getElements().get(0).getProcess().get(0).setName("PROCESSO");
			((ModelImpl)raiz).getElements().get(0).getQuestionnaire().get(0).setName("QUEST");
			buildRef(((ModelImpl)raiz).getElements().get(0).getExperiments(), "keyword");
			((KeywordImpl) ((ModelImpl)raiz).getElements().get(0).getExperiments().getKeyword().get(0)).setDescription("LOL");
			
			
			//exporta myexp
			Injector injector = createInjectorAndDoEMFRegistration();
			XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
			URI uri = URI.createURI("test."+factory.getEPackage().getNsPrefix());
			Resource xtextResource = resourceSet.createResource(uri);
			xtextResource.getContents().add(raiz);
			EcoreUtil.resolveAll(xtextResource);
			xtextResource.save(null);
			 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static  Injector createInjectorAndDoEMFRegistration() {
		org.eclipse.xtext.common.TerminalsStandaloneSetup.doSetup();

		Injector injector = createInjector();
		register(injector);
		return injector;
	}
	
	public static Injector createInjector() {
		return Guice.createInjector(new br.ufrn.dimap.ase.dsl.Expdslv3RuntimeModule());
	}
	
	public static void register(Injector injector) {
		if (!EPackage.Registry.INSTANCE.containsKey(factory.getEPackage().getNsURI())) {
			EPackage.Registry.INSTANCE.put(factory.getEPackage().getNsURI(), br.ufrn.dimap.ase.dsl.expdslv3.Expdslv3Package.eINSTANCE);
		}

		org.eclipse.xtext.resource.IResourceFactory resourceFactory = injector.getInstance(org.eclipse.xtext.resource.IResourceFactory.class);
		org.eclipse.xtext.resource.IResourceServiceProvider serviceProvider = injector.getInstance(org.eclipse.xtext.resource.IResourceServiceProvider.class);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(factory.getEPackage().getNsPrefix(), resourceFactory);
		org.eclipse.xtext.resource.IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put(factory.getEPackage().getNsPrefix(), serviceProvider);
	}

	private static void  buildRef(EObject raiz, String refName){
		String[] s = {refName};
		buildRefs(raiz, 1, s, null);
	}
	
	private static void  buildRef(EObject raiz, String refName, String specificDerivedClassName){
		String[] s = {refName};
		buildRefs(raiz, 1, s, specificDerivedClassName);
	}
	
	private static void  buildRefs(EObject raiz, int depth, String... filterNames){
		buildRefs(raiz, depth, filterNames, null);
	}

	@SuppressWarnings("unchecked")
	private static void  buildRefs(EObject raiz, int depth, String[] filterNames, String specificDerivedClassName){
		try {
			EClassImpl eclazz = (EClassImpl) raiz.eClass();
			for (EObject eObject : eclazz.eContents()) {
				if(eObject instanceof EReference) {
					if(filterNames == null || filterNames.length == 0 ||
					   Arrays.asList(filterNames).indexOf(((EReferenceImpl) eObject).getName()) != -1) {

						EObject ref = null;
						List<EClass> derivedClasses = findDerivedClasses(((EReferenceImpl) eObject).getEReferenceType());
						if(derivedClasses.size() > 0 && specificDerivedClassName != null && !specificDerivedClassName.isEmpty()) {
							for (EClass eClass : derivedClasses) {
								if(eClass.getName().equals(specificDerivedClassName)){
									ref= factory.create(eClass);	
									break;
								}
							}
						} else {
							ref= factory.create(((EReferenceImpl) eObject).getEReferenceType());							
						}
						

						//GET reference and add
						String refProperty = ((EReferenceImpl) eObject).getName();
						String methodGetName = "get"+Character.toUpperCase(refProperty.charAt(0)) + refProperty.substring(1);
						String methodSetName = "set"+Character.toUpperCase(refProperty.charAt(0)) + refProperty.substring(1);
						
						
						if(((EReferenceImpl) eObject).getUpperBound() == -1 || ((EReferenceImpl) eObject).getUpperBound() > 1) {
							Method mGet = raiz.getClass().getDeclaredMethod(methodGetName);
							EList<EObject> property = (EList<EObject>) mGet.invoke(raiz);
							property.add(ref);
						} else {
							Method mSet = raiz.getClass().getDeclaredMethod(methodSetName, ((EReferenceImpl) eObject).getEReferenceType().getInstanceClass());
							mSet.invoke(raiz, ref);
						}
						
						if(depth - 1 > 0) {
							buildRefs(ref, depth - 1, filterNames, specificDerivedClassName);
						}
					}
				}
			} 
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<EClass> findDerivedClasses(EClass eClass) {
		List<EClass> derivedClasses = new ArrayList<EClass>();
		EList<EObject> contents = factory.getEPackage().eContents();
		for (EObject eObject : contents) {
			if(eObject instanceof EClass) {
				EList<EClass> superTypes = ((EClass) eObject).getESuperTypes();
				if(superTypes.contains(eClass)) {
					derivedClasses.add((EClass) eObject);
				}
			}
		}
		return derivedClasses;
	}
}
