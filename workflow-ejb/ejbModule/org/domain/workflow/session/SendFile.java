package org.domain.workflow.session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.domain.model.processDefinition.ArtefactFile;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

@Name("sendFile")
@Restrict("#{identity.loggedIn}")
@Scope(ScopeType.EVENT)
public class SendFile {
	public void sendFile(ArtefactFile artefactFile){
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();

		try {
			File file = new File(artefactFile.getFile());
			byte[] data = Files.readAllBytes(file.toPath());  

			//response.setContentType("application/pdf");
			response.setContentLength(data.length);
			String name = artefactFile.getFile().split("/")[artefactFile.getFile().split("/").length-1];
			response.setHeader( "Content-disposition", "inline; filename="+name+"");

			ServletOutputStream out;
			out = response.getOutputStream();
			out.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		faces.responseComplete();
	}
}
