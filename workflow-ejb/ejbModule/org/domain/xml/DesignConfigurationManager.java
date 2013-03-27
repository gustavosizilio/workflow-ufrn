package org.domain.xml;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.domain.model.processDefinition.DesignType;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;
import org.domain.xml.util.LatinSquare;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DesignConfigurationManager extends XMLManager{
	
	public DesignConfigurationManager(String file, Workflow workflow) {
		this.file = file;
	}
	
	public Workflow executeTransformations(Workflow workflow) throws Exception{
		Element docEle = getDOM().getDocumentElement();
		if(getAttribute(docEle, Elements.TYPE).startsWith(DesignType.RCBD.toString())){
			executeTransformationsRCBD(workflow, docEle);
		} else if(getAttribute(docEle, Elements.TYPE).startsWith(DesignType.CRD.toString())){
			executeTransformationsCRD(workflow, docEle);
		} else if(getAttribute(docEle, Elements.TYPE).startsWith(DesignType.LS.toString())){
			executeTransformationsLS(workflow, docEle);
		}
		return  workflow;
	}

	private void executeTransformationsLS(Workflow workflow, Element docEle) throws Exception{
		workflow.setDesignType(DesignType.LS);
		List<Node> nodes = getElements(docEle.getChildNodes());
		int internalReplication;
		try {
			internalReplication = Integer.parseInt(getAttribute(docEle, Elements.INTERNAL_REPLICATION));
		} catch (Exception e) {
			internalReplication = 1;
		}
		
		Map<String,List<String>> notDesiredVariationFactors = new Hashtable<String,List<String>>();
		Map<String,List<String>> desiredVariationFactors = new Hashtable<String,List<String>>();
		Map<String, String> links = new Hashtable<String,String>();
		for (Node node : nodes) {
			if(getTagName(node).equals(Elements.FACTOR)){
				String nameFactor = getAttribute(node, Elements.NAME);
				String nameLevel = getAttribute(node, Elements.LEVEL);
				String isDesiredVariation = getAttribute(node, Elements.IS_DESIRED_VARIATION);
				if(isDesiredVariation.equals("False")){
					if(!notDesiredVariationFactors.containsKey(nameFactor)){
						notDesiredVariationFactors.put(nameFactor, new ArrayList<String>());
					}
					notDesiredVariationFactors.get(nameFactor).add(nameFactor+"."+nameLevel);
				}else if(isDesiredVariation.equals("True")){
					if(!desiredVariationFactors.containsKey(nameFactor)){
						desiredVariationFactors.put(nameFactor, new ArrayList<String>());
					}
					desiredVariationFactors.get(nameFactor).add(nameFactor+"."+nameLevel);
				}
			}else if(getTagName(node).equals(Elements.LINK)){
				String nameLink = getAttribute(node, Elements.NAME);
				String treatment = getAttribute(node, Elements.TREATMENT);
				links.put(nameLink, treatment);
			}
		}
		
		
		List<List<String>> listOfNotDesiredVariationFactors = new ArrayList<List<String>>(); //esperamos 2 fatores apenas...
		for (String s : notDesiredVariationFactors.keySet()) {
			listOfNotDesiredVariationFactors.add(notDesiredVariationFactors.get(s));
		}
		List<List<String>> listOfDesiredVariationFactors = new ArrayList<List<String>>(); //esperamos 1 fator apenas
		for (String s : desiredVariationFactors.keySet()) {
			listOfDesiredVariationFactors.add(desiredVariationFactors.get(s));
		}
		
		for (int i = 0; i < internalReplication; i++) {
			generateAssignmentsLS(workflow, links, listOfNotDesiredVariationFactors,
					listOfDesiredVariationFactors, i);
		}
	}

	private void generateAssignmentsLS(Workflow workflow, Map<String, String> links,
			List<List<String>> listOfNotDesiredVariationFactors,
			List<List<String>> listOfDesiredVariationFactors, int internalReplication) throws Exception {
		LatinSquare ls = new LatinSquare(
					listOfNotDesiredVariationFactors.get(0),
					listOfNotDesiredVariationFactors.get(1),
					listOfDesiredVariationFactors.get(0)
											);
		workflow.setTurnQuantity(listOfDesiredVariationFactors.get(0).size());
		createUserAssignmentsLs(workflow, ls, links, internalReplication);
	}

	private void createUserAssignmentsLs(Workflow workflow, LatinSquare ls, 
			Map<String, String> links, int internalReplication) {
		//List<UserAssignment> usersAssignments = new ArrayList<UserAssignment>();
		int size = ls.getLines().size();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				
				
				//find process
				String processName = findProcessName(ls, links, i, j);
				ProcessDefinition process = findProcess(workflow, processName);
				UserAssignment u = new UserAssignment(j,
						process, 
						ls.getLines().get(i),
						"LS "+internalReplication);
				//usersAssignments.add(u);
				process.getUserAssignments().add(u);
			}
		}
		
		//return usersAssignments;
	}

	private String findProcessName(LatinSquare ls, Map<String, String> links, int i, int j) {
		for (String key : links.keySet()) {
			List<String> s = new ArrayList<String>(); 
			for (String string : links.get(key).split(",")) {
				if(!string.isEmpty()){
					s.add(string);
				}
			}
			
			int hit = 0;
			for (String str : s) {
				if(str.equals(ls.getLines().get(i))){
					hit++;
				}
				if(str.equals(ls.getColumns().get(j))){
						hit++;
				}
				if(str.equals(ls.getTuples().get(i).get(j))){
						hit++;
				}
			}
			if(hit == s.size()){
				return key;
			}
		}
		return "";
	}

	private void executeTransformationsCRD(Workflow workflow, Element docEle) {
		workflow.setDesignType(DesignType.CRD);
		List<Node> nodes = getElements(docEle.getChildNodes());
		for (Node node : nodes) {
			if(getTagName(node).equals(Elements.PROCESS)){
				String subjectDescription = getAttribute(node, Elements.SUBJECT);
				ProcessDefinition process = findProcess(workflow, getAttribute(node, Elements.NAME));
				UserAssignment ua = new UserAssignment(subjectDescription, process);
				ua.setGroupValue("Subjects");
				process.getUserAssignments().add(ua);
			}
		}
	}

	private void executeTransformationsRCBD(Workflow workflow, Element docEle) {
		workflow.setDesignType(DesignType.RCBD);
		List<Node> nodes = getElements(docEle.getChildNodes());
		for (Node node : nodes) {
			if(getTagName(node).equals(Elements.PROCESS)){
				String subjectDescription = getAttribute(node, Elements.SUBJECT);
				ProcessDefinition process = findProcess(workflow, getAttribute(node, Elements.NAME));
				UserAssignment ua = new UserAssignment(subjectDescription, process);
				ua.setGroupValue(getAttribute(node, Elements.BLOCK));
				process.getUserAssignments().add(ua);
			}
		}
	}

	private ProcessDefinition findProcess(Workflow workflow, String attribute) {
		for (ProcessDefinition p : workflow.getProcessDefinitions()) {
			if(p.getName().equals(attribute)){
				return p;
			}
		}
		return null;
	}
	
}
