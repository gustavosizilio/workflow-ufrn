package org.domain.workflow.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.domain.dao.SeamDAO;
import org.domain.exception.ValidationException;
import org.domain.model.User;
import org.domain.model.processDefinition.Artefact;
import org.domain.model.processDefinition.ArtefactFile;
import org.domain.model.processDefinition.Field;
import org.domain.model.processDefinition.Observation;
import org.domain.model.processDefinition.ProcessDefinition;
import org.domain.model.processDefinition.TaskNode;
import org.domain.model.processDefinition.UserAssignment;
import org.domain.model.processDefinition.UserExecution;
import org.domain.model.processDefinition.Workflow;
import org.domain.model.processDefinition.metric.Questionnaire;
import org.domain.model.processDefinition.metric.UserAnswer;
import org.domain.ranalysis.ANOVA;
import org.domain.utils.PathBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

@Name("report")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.CONVERSATION)
public class WorkflowReport {
	private static final String WORKFLOW_REPORT_XHTML = "/workflow/report.xhtml";
	private static final String ANOVA_REPORT_XHTML = "/workflow/anova.xhtml";
	@In("seamDao") protected SeamDAO seamDao;
	@In("user") protected User user;
	private Workflow workflow;
	private Questionnaire currentQuestionnaire;
	@In(create=true, value="sendFile") protected SendFile sendFile;
	@In(value = "pathBuilder", create = true) PathBuilder pathBuilder;
	
	private Observation observation;
	
	@Begin(join=true, flushMode=FlushModeType.MANUAL)	
	public String init(Workflow workflow){
		this.setWorkflow(workflow);
		cleanObservation();		
		return WORKFLOW_REPORT_XHTML;
	}
	
	private ANOVA anova;
	public String analysis(){
		setAnova(new ANOVA());
		getAnova().build(this.workflow);
		return ANOVA_REPORT_XHTML;
	}

	private void cleanObservation() {
		this.setObservation(new Observation(this.workflow));
	}
	
	public void refreshData() {
		System.out.println(workflow.getId());
		this.workflow = seamDao.find(Workflow.class, workflow.getId());
		System.out.println(workflow.getId());
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
	
	
	public void downloadTaskResults() {
		try {
			Workbook wb = new HSSFWorkbook();
		    //Workbook wb = new XSSFWorkbook();
		    CreationHelper createHelper = wb.getCreationHelper();

		    
		    List<ProcessDefinition> procs = this.workflow.getProcessDefinitions();
		    for (ProcessDefinition processDefinition : procs) {
		    	Sheet sheet = wb.createSheet(processDefinition.getName());
			    Drawing drawing = sheet.createDrawingPatriarch();
			    
			    
			    List<UserAssignment> uas = processDefinition.getUserAssignments();  
			    int headerCold = 0;
			    for (int j=0; j<uas.size(); j++) {
			    	// Create a row and put some cells in it. Rows are 0 based.
				    Row row2 = sheet.createRow((short)j+1);
				    
				    // Create a cell and put a value in it.
				    List<String> factors = new ArrayList<String>();
				    factors.add(uas.get(j).getUser().toString());
				    
				    if(uas.get(j).getKeyFactors() != null && !uas.get(j).getKeyFactors().isEmpty()) {
				    	factors.addAll(Arrays.asList(uas.get(j).getKeyFactors().split("/")));
				    }
				    
				    headerCold = factors.size();
				    for (int i = 0; i < headerCold; i++) {
						String string = factors.get(i);
						Cell cell = row2.createCell(i);
						CellStyle style = wb.createCellStyle();
					    Font font = wb.createFont();
				        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				        style.setFont(font);
				        cell.setCellStyle(style);
					    cell.setCellValue(string);
					    
					    if(i==0) {
						    // Create the comment and set the text+author
						    ClientAnchor anchor = createHelper.createClientAnchor();
						    anchor.setCol1(cell.getColumnIndex());
						    anchor.setCol2(cell.getColumnIndex()+3);
						    anchor.setRow1(row2.getRowNum());
						    anchor.setRow2(row2.getRowNum()+7);
						    Comment comment = drawing.createCellComment(anchor);
						    comment.setString(createHelper.createRichTextString(uas.get(j).toString()));
		
						    // Assign the comment to the cell
						    cell.setCellComment(comment);
					    }
					    
					    sheet.autoSizeColumn(i);
					}
				    
				}
			    
			    
			    List<TaskNode> tasks = processDefinition.getTaskNodes();
			    Row row = sheet.createRow((short)0);
			    CellStyle style = wb.createCellStyle();
			    Font font = wb.createFont();
		        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		        style.setFont(font);
	            style.setWrapText(true);
	            row.setRowStyle(style);
	            int col = headerCold-1;
			    for (TaskNode taskNode : tasks) {
			    	Cell cell = row.createCell(++col);
				    //sheet.setColumnWidth(col, (int)100);
				    cell.setCellStyle(style);
				    cell.setCellValue(createHelper.createRichTextString(taskNode.getName()+ " - Start"));
				    for (int i=0; i<uas.size(); i++) {
				    	UserAssignment ua = uas.get(i);
				    	UserExecution ue = taskNode.getUserExecutionByUserAssignment(ua);
				    	if(ue != null) {
					    	Row r = sheet.getRow(i+1);
					    	Cell cellValue = r.createCell(col);
					    	CellStyle cellStyle = wb.createCellStyle();
					    	cellStyle.setWrapText(true);
					        cellStyle.setDataFormat(
					            createHelper.createDataFormat().getFormat("mm/dd/yy h:mm:ss"));
					        cellValue.setCellStyle(cellStyle);
					        
					    	if(ue.getStartedAt() != null)
					    		cellValue.setCellValue(ue.getStartedAt());
				    	}
					}
				    
				    
				    Cell cell2 = row.createCell(++col);
				    //sheet.setColumnWidth(col, (int)100);
				    cell2.setCellStyle(style);
				    cell2.setCellValue(createHelper.createRichTextString(taskNode.getName()+ " - End"));
				    for (int i=0; i<uas.size(); i++) {
				    	UserAssignment ua = uas.get(i);
				    	UserExecution ue = taskNode.getUserExecutionByUserAssignment(ua);
				    	if(ue != null) {
					    	Row r = sheet.getRow(i+1);
					    	Cell cellValue = r.createCell(col);
					    	CellStyle cellStyle = wb.createCellStyle();
					    	cellStyle.setWrapText(true);
					        cellStyle.setDataFormat(
					            createHelper.createDataFormat().getFormat("mm/dd/yy h:mm:ss"));
					        cellValue.setCellStyle(cellStyle);
					        
					    	if(ue.getFinishedAt() != null)
					    		cellValue.setCellValue(ue.getFinishedAt());
				    	}
					}
				    
				    Cell cell3 = row.createCell(++col);
				    //sheet.setColumnWidth(col, (int)100);
				    cell3.setCellStyle(style);
				    cell3.setCellValue(createHelper.createRichTextString(taskNode.getName()+ " - Time"));
				    for (int i=0; i<uas.size(); i++) {
				    	UserAssignment ua = uas.get(i);
				    	UserExecution ue = taskNode.getUserExecutionByUserAssignment(ua);
				    	if(ue != null) {
					    	Row r = sheet.getRow(i+1);
					    	Cell cellValue = r.createCell(col);
					    	CellStyle cellStyle = wb.createCellStyle();
					    	cellStyle.setWrapText(true);
					        cellValue.setCellStyle(cellStyle);
					        
					    	if(ue.getWastedTime() != null)
					    		cellValue.setCellValue(ue.getWastedTimeString());
				    	}
					}
			    	
			    	List<Field> fields = taskNode.getFields();
			    	for (Field field : fields) {
			    		Cell cell4 = row.createCell(++col);
					    //sheet.setColumnWidth(col, (int)100);
					    cell4.setCellStyle(style);
					    cell4.setCellValue(createHelper.createRichTextString(taskNode.getName()+ " - "+field.getName()));
					    for (int i=0; i<uas.size(); i++) {
					    	UserAssignment ua = uas.get(i);
					    	UserAnswer uanswer = field.getUserAssignmentAnswer(ua);
					    	if(uanswer != null) {
						    	Row r = sheet.getRow(i+1);
						    	Cell cellValue = r.createCell(col);
						    	CellStyle cellStyle = wb.createCellStyle();
						    	cellStyle.setWrapText(true);
						        cellValue.setCellStyle(cellStyle);
						        
						    	if(uanswer.getAnswer() != null)
						    		cellValue.setCellValue(uanswer.getAnswer());
					    	}
						}
					    
					}
			    	
			    	List<Artefact> artefacts = taskNode.getOutArtefacts();
			    	for (Artefact artefact : artefacts) {
			    		Cell cell5 = row.createCell(++col);
					    cell5.setCellStyle(style);
					    cell5.setCellValue(createHelper.createRichTextString(taskNode.getName()+ " - "+artefact.getName()));
					    for (int i=0; i<uas.size(); i++) {
					    	UserAssignment ua = uas.get(i);
					    	UserExecution ue = taskNode.getUserExecutionByUserAssignment(ua);
					    	ArtefactFile file = null;
					    	if(ue != null){
					    		file = artefact.get(ue);
					    	}
					    	if(file != null) {
						    	Row r = sheet.getRow(i+1);
						    	Cell cellValue = r.createCell(col);
						    	CellStyle cellStyle = wb.createCellStyle();
						    	
						    	Font hlink_font = wb.createFont();
						        hlink_font.setUnderline(Font.U_SINGLE);
						        hlink_font.setColor(IndexedColors.BLUE.getIndex());
						        cellStyle.setFont(hlink_font);
						       
						    	cellStyle.setWrapText(true);
						        cellValue.setCellStyle(cellStyle);
						        
						        
						        if(file.getFile() != null) {
						        	Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
							        link.setAddress(file.getFile().replace(pathBuilder.getExperimentDataPath(this.getWorkflow()), ""));
							        cellValue.setHyperlink(link);
						    		cellValue.setCellValue(file.getFile().replace(pathBuilder.getExperimentDataPath(this.getWorkflow()), ""));
						        }
					    	}
						}
					}
			    	
			    }
		    }
	
		    // Write the output to a file
		    String file = pathBuilder.getExperimentTaskResultsSheetPath(this.getWorkflow());
		    FileOutputStream fileOut = new FileOutputStream(file);
		    
			wb.write(fileOut);
			wb.close();
			fileOut.close();
			
			String zipFile = pathBuilder.getExperimentTaskResultsSheetZipPath(this.getWorkflow());
			
			ZipUtil zipUtil = new ZipUtil(zipFile);
			zipUtil.addDir(pathBuilder.getExperimentDataPath(this.getWorkflow()));
			zipUtil.addFile(pathBuilder.getExperimentPath(this.getWorkflow()), file);
			zipUtil.zipIt();
			
			sendFile.sendFile(zipFile);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
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

	public ANOVA getAnova() {
		return anova;
	}

	public void setAnova(ANOVA anova) {
		this.anova = anova;
	}

	public Questionnaire getCurrentQuestionnaire() {
		return currentQuestionnaire;
	}

	public void setCurrentQuestionnaire(Questionnaire currentQuestionnaire) {
		this.currentQuestionnaire = currentQuestionnaire;
	}
}
