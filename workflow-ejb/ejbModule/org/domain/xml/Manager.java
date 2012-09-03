package org.domain.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.Assignment;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Fork;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.StartState;
import org.domain.model.processDefinition.Swimlane;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.dataType.ArtefactType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Manager {
	
	private String file;
	
	public Manager(String file) {
		this.file = file;
	}
	
	public Document getDOM() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(this.file);
	}
	
	public List<ProcessDefinition> executeTransformations() throws ParserConfigurationException, SAXException, IOException{
		List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
		Element docEle = getDOM().getDocumentElement();
		NodeList nodes = docEle.getElementsByTagName(Elements.ELEMENTS).item(0).getChildNodes();
		
		for (int e = 0 ; e < nodes.getLength();e++) {
			if(getTagName(nodes.item(e)).equals(Elements.PROCESS_DEFINITION)){
				Node node = nodes.item(e);
				ProcessDefinition processDefinition = new ProcessDefinition(getAttribute(node, Elements.NAME));
				List<Node> nl = getElements(node.getChildNodes());
				if(nl != null && nl.size() > 0) {
					for(int i = 0 ; i < nl.size();i++) {
						if(nl.get(i).getNodeType() == 1){ //Somente do tipo ELEMENT
							extractElement(nl.get(i), processDefinition);
						}
					}
				}
				processDefinitions.add(processDefinition);
			}
		}
		
		return processDefinitions;
	}

	private void extractElement(Node item, ProcessDefinition processDefinition) {
		if(extraxtName(item.getNodeName()) == Elements.SWIMLANE){
			processDefinition.getSwimlanes().add(extractSwimlane(item, processDefinition));
		} else if (extraxtName(item.getNodeName()) == Elements.START_STATE){
			processDefinition.setStartState(extractStartState(item, processDefinition));
		} else if (extraxtName(item.getNodeName()) == Elements.TASK_NODE){
			processDefinition.getTaskNodes().add(extractTaskNode(item, processDefinition));
		} else if (extraxtName(item.getNodeName()) == Elements.JOIN){
			processDefinition.getJoins().add(extractJoin(item, processDefinition));
		} else if (extraxtName(item.getNodeName()) == Elements.FORK){
			processDefinition.getForks().add(extractFork(item, processDefinition));
		} else if (extraxtName(item.getNodeName()) == Elements.END_STATE){
			processDefinition.getEndStates().add(extractEndState(item, processDefinition));
		}
	}

	private EndState extractEndState(Node item, ProcessDefinition processDefinition) {
		EndState endState = new EndState();
		endState.setName(getAttribute(item, Elements.NAME));
		endState.setDescription(getAttribute(item, Elements.DESCRIPTION));
		endState.setProcessDefinition(processDefinition);
		return endState;
	}

	private Fork extractFork(Node item, ProcessDefinition processDefinition) {
		Fork fork = new Fork();
		fork.setName(getAttribute(item, Elements.NAME));
		fork.setProcessDefinition(processDefinition);
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if (extraxtName(node.getNodeName()) == Elements.TRANSITION){
				fork.getTransitions().add(extractTransition(node));
			}
		}
		
		return fork;
	}

	private Join extractJoin(Node item, ProcessDefinition processDefinition) {
		Join join = new Join();
		join.setName(getAttribute(item, Elements.NAME));
		join.setDescription(getAttribute(item, Elements.DESCRIPTION));
		join.setProcessDefinition(processDefinition);
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if (extraxtName(node.getNodeName()) == Elements.TRANSITION){
				Transition transition = extractTransition(node);
				transition.setJoin(join);
				join.getTransitions().add(transition);
			}
		}
		
		return join;
	}

	private TaskNode extractTaskNode(Node item, ProcessDefinition processDefinition) {
		TaskNode taskNode = new TaskNode();
		taskNode.setName(getAttribute(item, Elements.NAME));
		taskNode.setProcessDefinition(processDefinition);
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == Elements.TASK){
				Task task = extractTask(node);
				task.setTaskNode(taskNode);
				taskNode.getTasks().add(task);
			} else if (extraxtName(node.getNodeName()) == Elements.TRANSITION){
				taskNode.getTransitions().add(extractTransition(node));
			}
		}
		
		return taskNode;
	}

	private StartState extractStartState(Node item, ProcessDefinition processDefinition) {
		StartState startState = new StartState();
		startState.setName(getAttribute(item, Elements.NAME));
		startState.setProcessDefinition(processDefinition);
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == Elements.TASK){
				Task task = extractTask(node);
				task.setStartState(startState);
				startState.setTask(task);
			} else if (extraxtName(node.getNodeName()) == Elements.TRANSITION){
				startState.getTransitions().add(extractTransition(node));
			}
		}
		
		return startState;
	}

	private Transition extractTransition(Node item) {
		Transition transition = new Transition();
		transition.setName(getAttribute(item, Elements.NAME));
		transition.setDestination(getAttribute(item, Elements.TO));
		return transition;
	}

	private Task extractTask(Node item) {
		Task task = new Task();
		task.setName(getAttribute(item, Elements.NAME));
		task.setDescription(getAttribute(item, Elements.DESCRIPTION));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == Elements.ARTEFACTS){
				Artefact artefact = extractArtefact(node);
				artefact.setTask(task);
				task.getArtefacts().add(artefact);
			}
		}
		
		
		return task;
	}

	private Artefact extractArtefact(Node item) {
		Artefact artefact = new Artefact();
		artefact.setName(getAttribute(item, Elements.NAME));
		artefact.setArtefactType(ArtefactType.getValue(getAttribute(item, Elements.TYPE), ArtefactType.OUT));
		return artefact;
	}

	private Swimlane extractSwimlane(Node item, ProcessDefinition processDefinition) {
		Swimlane swimlane = new Swimlane();
		swimlane.setProcessDefinition(processDefinition);
		swimlane.setName(item.getAttributes().getNamedItem(Elements.NAME).getNodeValue());
		
		List<Node> node = getElements(item.getChildNodes());
		if(node.size() > 0){
			swimlane.setAssignment(extractAssignment(node.get(0)));//só pode haver 1 assignment
		}
		return swimlane;
	}
	
	private Assignment extractAssignment(Node item) {
		return new Assignment(
					(getAttribute(item, Elements.EXPRESSION)),
					(getAttribute(item, Elements.ACTOR_ID)),
					(getAttribute(item, Elements.POOLED_ACTORS))
				);
	}
	
	private String getAttribute(Node item, String attribute) {
		return (item.getAttributes().getNamedItem(attribute) != null ? item.getAttributes().getNamedItem(attribute).getNodeValue() : null);
	}
	private String getTagName(Node item) {
		return extraxtName(item.getNodeName());
	}
	
	private List<Node> getElements(NodeList nl) {
		List<Node> newNl = new ArrayList<Node>();
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				if(nl.item(i).getNodeType() == 1){ //Somente do tipo ELEMENT
					newNl.add(nl.item(i));
				}
			}
		}
		return newNl;
	}

	private String extraxtName(String nodeName) {
		if(nodeName.contains(":")){
			return nodeName.split(":")[1].toLowerCase(); 
		}else{
			return nodeName.toLowerCase(); 
		}
	}
}
