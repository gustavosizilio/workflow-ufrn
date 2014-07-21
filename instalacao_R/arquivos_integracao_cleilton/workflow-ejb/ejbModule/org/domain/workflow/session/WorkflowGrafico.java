package org.domain.workflow.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.domain.utils.FileObject;

import org.domain.core.RGeradorGrafico;
import org.domain.dao.SeamDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Observation;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.UserExecution;
import org.domain.model.processDefinition.Workflow;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import sun.util.calendar.CalendarUtils;

@Name("grafico")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowGrafico {
	private static final String WORKFLOW_REPORT_XHTML = "/workflow/grafico.xhtml";
	
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	private Workflow workflow;
	
	private RGeradorGrafico rGerador;
	
	List<FileObject> completeFileNames;
	
	private Observation observation;
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
	public String init(Workflow workflow){
		this.setWorkflow(workflow);
		cleanObservation();		
		
		gerarGrafico();
		
		return WORKFLOW_REPORT_XHTML;
	}

	private void gerarGrafico(){
		List<ProcessDefinition> definicoes = new ArrayList<ProcessDefinition>();
		List<String> fileNames = new ArrayList<String>();
		List<List<String>> medicoesPorResposta = new ArrayList<List<String>>();
		for (ProcessDefinition pd: workflow.getProcessDefinitions()){
			
			//String fileName = experimento.getId()+"_"+r.getId()+".png";
			String fileName = workflow.getId()+"_"+pd.getId()+".png";
			fileNames.add(fileName);
			
			definicoes.add(pd);
			
			ArrayList<String> medicaoResposta = new ArrayList<String>();
			
			for (TaskNode t: pd.getTaskNodes()){
				for (UserExecution uex: t.getUserExecutions()){

					Integer tempo = calcularDiferencaData(uex.getStartedAt(), uex.getFinishedAt());
					
					medicaoResposta.add(Integer.toString(tempo));
				}
			}
			
			medicoesPorResposta.add(medicaoResposta);
		}
		
		try{
			rGerador = new RGeradorGrafico(fileNames, definicoes, medicoesPorResposta);
			
			completeFileNames = new ArrayList<FileObject>();
			for (String s: fileNames){
				completeFileNames.add(new FileObject("../graficos_gerados/"+s));
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	private Integer calcularDiferencaData(Calendar c1, Calendar c2){
	  Integer hora1 = c1.get(Calendar.HOUR_OF_DAY);  
      Integer minuto1 = c1.get(Calendar.MINUTE);  
      Integer segundo1 = c1.get(Calendar.SECOND);  
        
      Integer qtdeSegundos1 = hora1 * 3600 + minuto1 * 60 + segundo1; 

      Integer hora2 = c2.get(Calendar.HOUR_OF_DAY);  
      Integer minuto2 = c2.get(Calendar.MINUTE);  
      Integer segundo2 = c2.get(Calendar.SECOND);  
        
      Integer qtdeSegundos2 = hora2 * 3600 + minuto2 * 60 + segundo2; 
      
      return qtdeSegundos2 - qtdeSegundos1;

	}
	
	private void cleanObservation() {
		this.setObservation(new Observation(this.workflow));
	}
	
	public void wire(){
		seamDao.refresh(workflow);
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}
	
	public void saveObservation(){
		try {
			this.observation.setCreatedAt(new GregorianCalendar());
			seamDao.persist(this.observation);
			this.workflow.getObservations().add(this.observation);
			seamDao.merge(this.workflow);
			seamDao.flush();
			cleanObservation();
		} catch (ValidationException e) {
		}
	}
	public void removeObservation(Observation o){
		try {
			this.workflow.getObservations().remove(o);
			seamDao.remove(o);
			seamDao.merge(this.workflow);
			seamDao.flush();
			cleanObservation();
		} catch (ValidationException e) {
		}
	}
	
	public String getPathGrafico(){
		return "../graficos_gerados/Teste.png";
	}

	public RGeradorGrafico getrGerador() {
		return rGerador;
	}

	public void setrGerador(RGeradorGrafico rGerador) {
		this.rGerador = rGerador;
	}
}
