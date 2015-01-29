package org.domain.dataManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.domain.dao.SeamDAO;
import org.domain.exception.ValidationException;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.EndState;
import org.domain.model.processDefinition.Fork;
import org.domain.model.processDefinition.Join;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.StartState;
import org.domain.model.processDefinition.Task;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.Transition;
import org.domain.model.processDefinition.Workflow;
import org.domain.model.processDefinition.dataType.ArtefactType;
import org.domain.model.processDefinition.metric.Question;
import org.domain.model.processDefinition.metric.QuestionOption;
import org.domain.model.processDefinition.metric.QuestionType;
import org.domain.model.processDefinition.metric.Questionnaire;
import org.domain.model.processDefinition.metric.QuestionnaireType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WorkflowManager extends XMLManager{
	private Workflow workflow;
	private SeamDAO seamDao;
	
	public WorkflowManager(String file, Workflow workflow, SeamDAO seamDAO) {
		super();
		this.workflow = workflow;
		this.seamDao = seamDAO;
		this.file = file;
	}
	private Map<String, Questionnaire> questionnaires; 
	private List<ProcessDefinition> processDefinitions;
	public void executeTransformations() throws ParserConfigurationException, SAXException, IOException, ValidationException{
		extractQuestionnaires();
		//extractMetrics();
		extractProcessDefinitions();
	}

	/*private void extractMetrics() throws ParserConfigurationException, SAXException, IOException, ValidationException {
		metrics = new HashMap<String, Metric>();
		
		Element docEle = getDOM().getDocumentElement();
		NodeList nodes = docEle.getElementsByTagName(Elements.ELEMENTS).item(0).getChildNodes();
		for (int e = 0 ; e < nodes.getLength();e++) {
			if(getTagName(nodes.item(e)).equals(Elements.METRICS)){
				Node item = nodes.item(e);
				Metric metric = new Metric();
				metric.setName(getAttribute(item, Elements.NAME));
				if(!metrics.containsKey(metric.getName())){
					metric.setRefName(getAttribute(item, Elements.REFNAME));
					if(getAttribute(item, Elements.TYPE) != null && getAttribute(item, Elements.TYPE).equals("quest")){
						metric.setMetricType(MetricType.QUEST);
						metric.setQuestionnaire(this.questionnaires.get(metric.getRefName()));
					} else if (getAttribute(item, Elements.TYPE) != null && getAttribute(item, Elements.TYPE).equals("collectedData")) {
						metric.setMetricType(MetricType.COLLECTED_DATA);
					} else if (getAttribute(item, Elements.TYPE) != null && getAttribute(item, Elements.TYPE).equals("time")) {
						metric.setMetricType(MetricType.TIME);
					} else {
						metric.setMetricType(MetricType.COLLECTED_DATA);
					}
					seamDao.persist(metric);
					metrics.put(metric.getName(), metric);
				}
				metrics.put(metric.getName(), metric);
			}
		}
		;
		for (Metric m : metrics.values()) {
			this.seamDao.persist(m);
		}
	    seamDao.flush();
	}
	*/
	
	private void extractQuestionnaires() throws ParserConfigurationException, SAXException, IOException, ValidationException {
		questionnaires = new HashMap<String, Questionnaire>();
		
		Element docEle = getDOM().getDocumentElement();
		NodeList nodes = docEle.getElementsByTagName(Elements.ELEMENTS).item(0).getChildNodes();
		for (int e = 0 ; e < nodes.getLength();e++) {
			if(getTagName(nodes.item(e)).equals(Elements.QUESTIONNAIRES)){
				Node node = nodes.item(e);
				Questionnaire questionnaire = new Questionnaire(getAttribute(node, Elements.NAME));
				questionnaire.setQuestionnaireType(QuestionnaireType.METRIC_LINKED);
				
				String process = getAttribute(node, Elements.PROCESS);
				String type = getAttribute(node, Elements.TYPE);
				if(process != null){
					questionnaire.setQuestionnaireType(QuestionnaireType.PRE_PROCESS);
					questionnaire.setProcessName(process);
					if(type != null) {
						if(type.equals(Elements.POST))
							questionnaire.setQuestionnaireType(QuestionnaireType.POST_PROCESS);
						else
							questionnaire.setQuestionnaireType(QuestionnaireType.PRE_PROCESS);
					}
				} else {
					if(type != null) {
						if(type.equals(Elements.POST))
							questionnaire.setQuestionnaireType(QuestionnaireType.POST_EXPERIMENT);
						else
							questionnaire.setQuestionnaireType(QuestionnaireType.PRE_EXPERIMENT);
					}
				}
				
				List<Node> nl = getElements(node.getChildNodes());
				if(nl != null && nl.size() > 0) {
					for(int i = 0 ; i < nl.size();i++) {
						if(nl.get(i).getNodeType() == 1){ //Somente do tipo ELEMENT
							extractElement(nl.get(i), questionnaire);
						}
					}
				}
				questionnaires.put(questionnaire.getName(), questionnaire);
			}
		}
		;
		for (Questionnaire questionnaire : questionnaires.values()) {
			this.seamDao.persist(questionnaire);
		}
		this.workflow.getQuestionnaires().addAll(questionnaires.values());
	    seamDao.merge(this.workflow);
	    seamDao.flush();
	}

	private void extractElement(Node item, Questionnaire questionnaire) {
		if (extraxtName(item.getNodeName()).equals(Elements.QUESTION)){
			questionnaire.getQuestions().add(extractQuestion(item, questionnaire));
		}
	}

	private Question extractQuestion(Node item, Questionnaire questionnaire) {
		Question question = new Question();
		question.setDescription(getAttribute(item, Elements.DESCRIPTION));
		question.setQuestionnaire(questionnaire);
		if(getAttribute(item, Elements.TYPE) != null){
			if(getAttribute(item, Elements.TYPE).equals(Elements.COMBOBOX)){
				question.setType(QuestionType.COMBOBOX);
			}
			if(getAttribute(item, Elements.TYPE).equals(Elements.CHECKBOX)){
				question.setType(QuestionType.CHECKBOX);
			}
			if(getAttribute(item, Elements.TYPE).equals(Elements.TEXT)){
				question.setType(QuestionType.TEXT);
			}
			if(getAttribute(item, Elements.TYPE).equals(Elements.PARAGRAPHTEXT)){
				question.setType(QuestionType.PARAGRAPH);
			}
		} else {
			question.setType(QuestionType.TEXT);
		}
		
		List<Node> nodes = getElements(item.getChildNodes());
		for (Node node : nodes) {
			if(extraxtName(node.getNodeName()).equals(Elements.OPTION)){
				QuestionOption option = extractOption(node, question);
				question.getOptions().add(option);
			}
		}
		return question;
	}

	private QuestionOption extractOption(Node item, Question question) {
		QuestionOption option = new QuestionOption();
		option.setDescription(getAttribute(item, Elements.DESCRIPTION));
		option.setQuestion(question);
		return option;
	}

	private void extractProcessDefinitions() throws ParserConfigurationException, SAXException,
			IOException, ValidationException {
		setProcessDefinitions(new ArrayList<ProcessDefinition>());
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
				getProcessDefinitions().add(processDefinition);
			}
		}
		
		
		List<ProcessDefinition> processDefinitions = getProcessDefinitions();
		for (ProcessDefinition process : processDefinitions) {
			process.setWorkflow(this.workflow);
			this.seamDao.persist(process);
			setQuestionnairesToProcess(process);
		}
		this.workflow.getProcessDefinitions().addAll(processDefinitions);
		setQuestionnairesToWorkflow(this.workflow);
	    seamDao.merge(this.workflow);
	    seamDao.flush();
	}

	private void setQuestionnairesToWorkflow(Workflow workflow2) throws ValidationException {
		for (Questionnaire q : questionnaires.values()) {
			if((q.getQuestionnaireType().equals(QuestionnaireType.POST_EXPERIMENT) || q.getQuestionnaireType().equals(QuestionnaireType.PRE_EXPERIMENT))){
				q.setWorkflow(workflow2);
				seamDao.merge(q);
				workflow2.getQuestionnaires().add(q);
			}
		}
		seamDao.merge(workflow2);
	}

	private void setQuestionnairesToProcess(ProcessDefinition process) throws ValidationException {
		for (Questionnaire q : questionnaires.values()) {
			if((q.getQuestionnaireType().equals(QuestionnaireType.POST_PROCESS) || q.getQuestionnaireType().equals(QuestionnaireType.PRE_PROCESS)) && q.getProcessName() != null && q.getProcessName().equals(process.getName())){
				q.setProcess(process);
				seamDao.merge(q);
				process.getQuestionnaires().add(q);
			}
		}
		seamDao.merge(process);
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
			/*if(extraxtName(node.getNodeName()).equals(Elements.METRICS)){
				Metric metric = metrics.get(getAttribute(node, Elements.NAME));
				metric.getTaskNodes().add(taskNode);
				taskNode.getMetrics().add(metric);
			}*/
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

	public List<ProcessDefinition> getProcessDefinitions() {
		return processDefinitions;
	}

	public void setProcessDefinitions(List<ProcessDefinition> processDefinitions) {
		this.processDefinitions = processDefinitions;
	}

}
