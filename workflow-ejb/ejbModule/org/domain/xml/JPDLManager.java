package org.domain.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.domain.model.processDefinition.Action;
import org.domain.model.processDefinition.Assignment;
import org.domain.model.processDefinition.Controller;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Event;
import org.domain.model.processDefinition.Fork;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.StartState;
import org.domain.model.processDefinition.Swimlane;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.Variable;
import org.domain.model.processDefinition.dataType.BooleanType;
import org.domain.model.processDefinition.dataType.PriorityType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JPDLManager {
	
	private String file;
	
	public JPDLManager(String file) {
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
		NodeList nodes = docEle.getChildNodes();
		
		for (int e = 0 ; e < nodes.getLength();e++) {
			if(getTagName(nodes.item(e)).equals(JPDLElements.PROCESS_DEFINITION)){
				Node node = nodes.item(e);
				ProcessDefinition processDefinition = new ProcessDefinition(getAttribute(node, JPDLElements.NAME));
				System.out.println("um process "+processDefinition.getName());
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
		if(extraxtName(item.getNodeName()) == JPDLElements.SWIMLANE){
			processDefinition.getSwimlanes().add(extractSwimlane(item));
		} else if (extraxtName(item.getNodeName()) == JPDLElements.START_STATE){
			processDefinition.setStartState(extractStartState(item));
		} else if (extraxtName(item.getNodeName()) == JPDLElements.TASK_NODE){
			processDefinition.getTaskNodes().add(extractTaskNode(item));
		} else if (extraxtName(item.getNodeName()) == JPDLElements.JOIN){
			processDefinition.getJoins().add(extractJoin(item));
		} else if (extraxtName(item.getNodeName()) == JPDLElements.FORK){
			processDefinition.getForks().add(extractFork(item));
		} else if (extraxtName(item.getNodeName()) == JPDLElements.END_STATE){
			processDefinition.getEndStates().add(extractEndState(item));
		}
	}

	private EndState extractEndState(Node item) {
		EndState endState = new EndState();
		endState.setName(getAttribute(item, JPDLElements.NAME));
		return endState;
	}

	private Fork extractFork(Node item) {
		Fork fork = new Fork();
		fork.setName(getAttribute(item, JPDLElements.NAME));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if (extraxtName(node.getNodeName()) == JPDLElements.TRANSITION){
				fork.getTransitions().add(extractTransition(node));
			}
		}
		
		return fork;
	}

	private Join extractJoin(Node item) {
		Join join = new Join();
		join.setName(getAttribute(item, JPDLElements.NAME));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if (extraxtName(node.getNodeName()) == JPDLElements.TRANSITION){
				join.getTransitions().add(extractTransition(node));
			}
		}
		
		return join;
	}

	private TaskNode extractTaskNode(Node item) {
		TaskNode taskNode = new TaskNode();
		taskNode.setName(getAttribute(item, JPDLElements.NAME));
		taskNode.setCreateTasks(BooleanType.getValue(getAttribute(item, JPDLElements.CREATE_TASKS), true));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == JPDLElements.TASK){
				taskNode.getTasks().add(extractTask(node));
			} else if (extraxtName(node.getNodeName()) == JPDLElements.TRANSITION){
				taskNode.getTransitions().add(extractTransition(node));
			} else if (extraxtName(node.getNodeName()) == JPDLElements.EVENT){
				taskNode.getEvents().add(extractEvent(node));
			}
		}
		
		return taskNode;
	}

	private StartState extractStartState(Node item) {
		StartState startState = new StartState();
		startState.setName(getAttribute(item, JPDLElements.NAME));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == JPDLElements.TASK){
				startState.setTask(extractTask(node));
			} else if (extraxtName(node.getNodeName()) == JPDLElements.TRANSITION){
				startState.getTransitions().add(extractTransition(node));
			} else if (extraxtName(node.getNodeName()) == JPDLElements.EVENT){
				startState.getEvents().add(extractEvent(node));
			}
		}
		
		return startState;
	}

	private Event extractEvent(Node item) {
		Event event = new Event();
		event.setType(getAttribute(item, JPDLElements.TYPE));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == JPDLElements.ACTION){
				event.getActions().add(extractAction(node));
			} 
		}
		return event;
	}

	private Action extractAction(Node item) {
		Action action = new Action();
		action.setName(getAttribute(item, JPDLElements.NAME));
		action.setClazz(getAttribute(item, JPDLElements.CLASS));
		return action;
	}

	private Transition extractTransition(Node item) {
		Transition transition = new Transition();
		transition.setName(getAttribute(item, JPDLElements.NAME));
		transition.setDestination(getAttribute(item, JPDLElements.TO));
		return transition;
	}

	private Task extractTask(Node item) {
		Task task = new Task();
		task.setName(getAttribute(item, JPDLElements.NAME));
		task.setDescription(getAttribute(item, JPDLElements.DESCRIPTION));
		/*task.setBlocking(BooleanType.getValue(getAttribute(item, JPDLElements.BLOCKING), false));
		task.setSignalling(BooleanType.getValue(getAttribute(item, JPDLElements.SIGNALLING), true));
		task.setDescription(getAttribute(item, JPDLElements.DESCRIPTION));
		task.setDuedate(getAttribute(item, JPDLElements.DUEDATE));
		task.setSwimlane(getAttribute(item, JPDLElements.SWIMLANE));*/
		task.setPriority(PriorityType.getValue(getAttribute(item, JPDLElements.PRIORITY), PriorityType.normal));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == JPDLElements.CONTROLLER){
				task.setController(extractController(node));
			}
		}
		
		
		return task;
	}

	private Controller extractController(Node item) {
		Controller controller = new Controller();
		/*controller.setClazz(getAttribute(item, JPDLElements.CLASS));
		controller.setConfigType(ConfigType.getValue(getAttribute(item, JPDLElements.CONFIG_TYPE),null));*/ 
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()) == JPDLElements.VARIABLE){
				controller.getVariables().add(extractVariable(node));
			}
		}
		return controller;
	}

	private Variable extractVariable(Node item) {
		Variable variable = new Variable();
		variable.setName(getAttribute(item, JPDLElements.NAME));
		//variable.setMappedName(getAttribute(item, JPDLElements.MAPPED_NAME));
		variable.setAccess(getAttribute(item, JPDLElements.ACCESS));
		return variable;
	}

	private Swimlane extractSwimlane(Node item) {
		Swimlane swimlane = new Swimlane();
		swimlane.setName(item.getAttributes().getNamedItem(JPDLElements.NAME).getNodeValue());
		
		List<Node> node = getElements(item.getChildNodes());
		if(node.size() > 0){
			swimlane.setAssignment(extractAssignment(node.get(0)));//só pode haver 1 assignment
		}
		return swimlane;
	}
	
	private Assignment extractAssignment(Node item) {
		return new Assignment(
					(getAttribute(item, JPDLElements.EXPRESSION)),
					(getAttribute(item, JPDLElements.ACTOR_ID)),
					(getAttribute(item, JPDLElements.POOLED_ACTORS))
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
