package org.domain.xml;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.domain.model.processDefinition.DesignType;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.Workflow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DesignConfigurationManager extends XMLManager{
	
	private String file;
	
	public DesignConfigurationManager(String file, Workflow workflow) {
		this.file = file;
	}
	
	public Document getDOM() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(this.file);
	}
	
	public Workflow executeTransformations(Workflow workflow) throws ParserConfigurationException, SAXException, IOException{
		Element docEle = getDOM().getDocumentElement();
		if(getAttribute(docEle, Elements.TYPE).startsWith(DesignType.RCBD.toString())){
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
		} else if(getAttribute(docEle, Elements.TYPE).startsWith(DesignType.CDR.toString())){
		 	System.out.println("Entrou aqui CDR!");
		} else if(getAttribute(docEle, Elements.TYPE).startsWith(DesignType.LS.toString())){
		 	System.out.println("Entrou aqui! LS");
		}
		return  workflow;
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
