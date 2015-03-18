package org.domain.dataManager;

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
		String treatment = null;
		String col = null;
		String row = null;
		
		try {
			internalReplication = Integer.parseInt(getAttribute(docEle, Elements.INTERNAL_REPLICATION));
		} catch (Exception e) {
			internalReplication = 1;
		}
		
		List<String> colFactors = new ArrayList<String>();
		List<String> rowFactors = new ArrayList<String>();
		List<String> treatmentFactors = new ArrayList<String>();
		Map<String, String> links = new Hashtable<String,String>();
		
		for (Node node : nodes) {
			if (getTagName(node).equals(Elements.LS)) {
				treatment = getAttribute(node, Elements.TREATMENT);
				col = getAttribute(node, Elements.COL);
				row = getAttribute(node, Elements.ROW);
			} else if(getTagName(node).equals(Elements.FACTOR)){
				String nameFactor = getAttribute(node, Elements.NAME);
				String nameLevel = getAttribute(node, Elements.LEVEL);
				if(nameFactor.equals(treatment)){
					treatmentFactors.add(nameFactor +"."+ nameLevel);
				} else if(nameFactor.equals(col)){
					colFactors.add(nameFactor +"."+ nameLevel);
				} else if(nameFactor.equals(row)){
					rowFactors.add(nameFactor +"."+ nameLevel);
				}
			}else if(getTagName(node).equals(Elements.LINK)){
				//TODO VERIFY
				String nameLink = getAttribute(node, Elements.NAME);
				String treatment2 = getAttribute(node, Elements.TREATMENT);
				links.put(nameLink, treatment2);
			}
		}
		
		
		for (int i = 0; i < internalReplication; i++) {
			generateAssignmentsLS(workflow, links, treatmentFactors,
					colFactors, rowFactors,  i);
		}
	}

	private void generateAssignmentsLS(Workflow workflow, Map<String, String> links,
			List<String> treatmentFactors, List<String> colFactors, List<String> rowFactors,
			int internalReplication) throws Exception {
		LatinSquare ls = new LatinSquare(
							rowFactors,
							colFactors,
							treatmentFactors
						);
		
		workflow.setTurnQuantity(treatmentFactors.size());
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
						"LS "+internalReplication,
						ls.getLines().get(i) + "/" + ls.getColumns().get(j) + "/" + ls.getTuples().get(i).get(j));
				process.getUserAssignments().add(u);
			}
		}
		
		//return usersAssignments;
	}
	
	private String findProcessName(Map<String, String> links, String s1, String s2) {
		for (String key : links.keySet()) {
			List<String> s = new ArrayList<String>(); 
			for (String string : links.get(key).split(",")) {
				if(!string.isEmpty()){
					s.add(string);
				}
			}
			
			int hit = 0;
			for (String str : s) {
				if(str.equals(s1)){
					hit++;
				}
				if(str.equals(s2)){
					hit++;
				}
			}
			if(hit == s.size()){
				return key;
			}
		}
		return "";
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
				//TODO NULL
				UserAssignment ua = new UserAssignment(subjectDescription, process, null);
				ua.setGroupValue("Subjects");
				process.getUserAssignments().add(ua);
			}
		}
	}

	private void executeTransformationsRCBD(Workflow workflow, Element docEle) throws Exception {
		workflow.setDesignType(DesignType.RCBD);
		List<Node> nodes = getElements(docEle.getChildNodes());
		int internalReplication;
		String treatment = null;
		String block = null;
		
		try {
			internalReplication = Integer.parseInt(getAttribute(docEle, Elements.INTERNAL_REPLICATION));
		} catch (Exception e) {
			internalReplication = 1;
		}
		
		List<String> treatmentFactors = new ArrayList<String>();
		List<String> blockFactors = new ArrayList<String>();
		Map<String, String> links = new Hashtable<String,String>();
		
		for (Node node : nodes) {
			if (getTagName(node).equals(Elements.RCBD)) {
				treatment = getAttribute(node, Elements.TREATMENT);
				block = getAttribute(node, Elements.BLOCK);
			} else if(getTagName(node).equals(Elements.FACTOR)){
				String nameFactor = getAttribute(node, Elements.NAME);
				String nameLevel = getAttribute(node, Elements.LEVEL);
				if(nameFactor.equals(treatment)){
					treatmentFactors.add(nameFactor +"."+ nameLevel);
				} else if(nameFactor.equals(block)){
					blockFactors.add(nameFactor +"."+ nameLevel);
				}
			}else if(getTagName(node).equals(Elements.LINK)){
				//TODO VERIFY
				String nameLink = getAttribute(node, Elements.NAME);
				String treatment2 = getAttribute(node, Elements.TREATMENT);
				links.put(nameLink, treatment2);
			}
		}
		
		generateAssignmentsRCBD(workflow, links, treatmentFactors, blockFactors,  internalReplication);
		
	}

	private void generateAssignmentsRCBD(Workflow workflow, Map<String, String> links,
			List<String> treatmentFactors, List<String> blockFactors, int internalReplication) throws Exception {
		
		int n = 1;
		for (int i = 0; i < blockFactors.size(); i++) {
			for (int j = 0; j < treatmentFactors.size(); j++) {
				for (int x = 0; x < internalReplication; x++) {
					//find process
					String processName = findProcessName(links, blockFactors.get(i), treatmentFactors.get(j));
					ProcessDefinition process = findProcess(workflow, processName);
					
					UserAssignment u = new UserAssignment(0,
							process, 
							"Subject "+n,
							blockFactors.get(i),
							blockFactors.get(i) + "/" + treatmentFactors.get(j));
					process.getUserAssignments().add(u);
					
					n++;
				}	
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
