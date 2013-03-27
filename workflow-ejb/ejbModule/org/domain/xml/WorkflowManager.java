package org.domain.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Fork;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.StartState;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.dataType.ArtefactType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WorkflowManager extends XMLManager{
	
	public WorkflowManager(String file) {
		super();
		this.file = file;
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
		if (extraxtName(item.getNodeName()).equals(Elements.START_STATE)){
			processDefinition.setStartState(extractStartState(item, processDefinition));
		} else if (extraxtName(item.getNodeName()).equals(Elements.TASK_NODE)){
			processDefinition.getTaskNodes().add(extractTaskNode(item, processDefinition));
		} else if (extraxtName(item.getNodeName()).equals(Elements.JOIN)){
			processDefinition.getJoins().add(extractJoin(item, processDefinition));
		} else if (extraxtName(item.getNodeName()).equals(Elements.FORK)){
			processDefinition.getForks().add(extractFork(item, processDefinition));
		} else if (extraxtName(item.getNodeName()).equals(Elements.END_STATE)){
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
			if (extraxtName(node.getNodeName()).equals(Elements.TRANSITION)){
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
			if (extraxtName(node.getNodeName()).equals(Elements.TRANSITION)){
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
		taskNode.setDescription(getAttribute(item, Elements.DESCRIPTION));
		taskNode.setProcessDefinition(processDefinition);
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()).equals(Elements.TASK)){
				Task task = extractTask(node);
				task.setTaskNode(taskNode);
				taskNode.getTasks().add(task);
			} else if (extraxtName(node.getNodeName()).equals(Elements.TRANSITION)){
				Transition transition = extractTransition(node);
				transition.setTaskNode(taskNode);
				taskNode.getTransitions().add(transition);
			}
			if(extraxtName(node.getNodeName()).equals(Elements.ARTEFACTS)){
				Artefact artefact = extractArtefact(node);
				artefact.setTaskNode(taskNode);
				taskNode.getArtefacts().add(artefact);
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
			if(extraxtName(node.getNodeName()).equals(Elements.TASK_NODE)){
				TaskNode taskNode = extractTaskNode(node, processDefinition);
				taskNode.setStartState(startState);
				startState.setTaskNode(taskNode);
			} else if (extraxtName(node.getNodeName()).equals(Elements.TRANSITION)){
				startState.getTransitions().add(extractTransition(node));
			}
		}
		
		return startState;
	}

	private Transition extractTransition(Node item) {
		Transition transition = new Transition();
		transition.setName(getAttribute(item, Elements.NAME));
		transition.setDestination(getAttribute(item, Elements.TO));
		transition.setDescription(getAttribute(item, Elements.DESCRIPTION));
		return transition;
	}

	private Task extractTask(Node item) {
		Task task = new Task();
		task.setName(getAttribute(item, Elements.NAME));
		task.setDescription(getAttribute(item, Elements.DESCRIPTION));
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()).equals(Elements.ARTEFACTS)){
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

}
