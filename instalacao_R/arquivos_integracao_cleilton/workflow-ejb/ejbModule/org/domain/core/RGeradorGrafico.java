package org.domain.core;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.domain.model.processDefinition.ProcessDefinition;
import org.rosuda.JRI.Rengine;

public class RGeradorGrafico {
	public static final String[] args = {};
	public static final Rengine r = new Rengine(args, false, null);
	
	private static String DESTINATION_DIR_PATH ="C:/Users/Cleilton/Desktop/ImagensR/";
	
	public RGeradorGrafico(List<String> fileNames, List<ProcessDefinition> listaDefinicoesProcessos, List<List<String>> medicoesPorResposta) {
		gerarGraficoDispersao(fileNames, listaDefinicoesProcessos, medicoesPorResposta);	
	}
	
	

	private void gerarGraficoDispersao(List<String> fileNames, List<ProcessDefinition> listaDefinicoesProcessos, List<List<String>> medicoesPorResposta) {

		Iterator<String> iFileNames = fileNames.iterator();
		Iterator<ProcessDefinition> iDefinicoes = listaDefinicoesProcessos.iterator();
		Iterator<List<String>> iMedicoesPorResposta = medicoesPorResposta.iterator();
		
		FacesContext aFacesContext = FacesContext.getCurrentInstance();
		ServletContext context = (ServletContext) aFacesContext.getExternalContext().getContext();
		String realPath = context.getRealPath("/graficos_gerados/");
		realPath = backlashReplace(realPath);
		while (iDefinicoes.hasNext()){
			
			ProcessDefinition definicaoProcesso = iDefinicoes.next();
			
			String title = definicaoProcesso.getName();
			String xlab = "Tratamentos";
			String ylab = "Tempo (Segundos)";
			
			String listaMedicao = "";
			String listaProvas = "";
			
			Iterator<String> iMedicaoResposta = iMedicoesPorResposta.next().iterator();
			
			int contador = 0;
			while(iMedicaoResposta.hasNext()){
				
				String medicao = iMedicaoResposta.next();
				
				if (contador == 0){
					listaMedicao += medicao;
					//listaProvas += medicao.getProva().getTratamento().getNumTratamento();
					
					listaProvas += contador;
				
				} else {
					listaMedicao += ","+medicao;
					//listaProvas += ","+medicao.getProva().getTratamento().getNumTratamento();
					
					listaProvas += ","+contador;
				}
				
				contador++;
				
			}		
			
			r.eval("listaMedicao<-c("+listaMedicao+")");
			r.eval("listaProvas<-c("+listaProvas+")");
				
			System.out.println(r.eval("a"));
			
			r.eval("png(file=\""+realPath+iFileNames.next()+"\",width=1600,height=1600,res=400)");
			r.eval("plot(listaProvas,listaMedicao,col=\"Blue\",main=\"" + title + "\",xlab=\""
			     + xlab + "\",ylab=\"" + ylab + "\")");
			//r.eval("dev.copy(png,\""+fileName+"\")");
			r.eval("dev.off()");
			
			
		}  
		
	}

	/*public static void gerarGrafico(String fileName, Collection<RegistroResposta> listaRespostas) throws IOException{

		String title = "R Plot in JFrame";
		String xlab = "Provas";
		String ylab = "Respostas";
		       
		FacesContext aFacesContext = FacesContext.getCurrentInstance();
		ServletContext context = (ServletContext) aFacesContext.getExternalContext().getContext();
		String real = context.getRealPath("/graficos_gerados/");
				
		String listaA = "";
		String listaB = "";
		
		Iterator<RegistroResposta> iterator = listaRespostas.iterator();
		
		int contador = 0;
		while(iterator.hasNext()){
			
			RegistroResposta elemento = iterator.next();
			
			if (contador == 0){
				listaA += elemento.getValor();
				listaB += ++contador;
			} else {
				listaA += ","+elemento.getValor();
				listaB += ","+(++contador);
			}
			
		}		
		real = backlashReplace(real);
		
        r.eval("a<-c("+listaA+")");
		r.eval("b<-c("+listaB+")");
		System.out.println(r.eval("a"));
		r.eval("png(file=\""+real+fileName+"\",width=1600,height=1600,res=400)");
		r.eval("plot(a,b,col=\"Blue\",main=\"" + title + "\",xlab=\""
		     + xlab + "\",ylab=\"" + ylab + "\")");
		//r.eval("dev.copy(png,\""+fileName+"\")");
		r.eval("dev.off()");
		
		
		
		//File file = new File(DESTINATION_DIR_PATH, fileName);
		//Image image = ImageIO.read(file);
		//BufferedImage imagem = ImageIO.read(file);
		// fazer algo com a imagem...
		//ImageIO.write(imagem, "PNG", new File(fileName));
        
		
		 	File destinationDir = new File(DESTINATION_DIR_PATH, fileName);
			OutputStream out = new FileOutputStream(destinationDir);
			InputStream filecontent = imagem.
				filePart.getInputStream();
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = filecontent.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}			
			filecontent.close();
			out.flush();
			out.close();

		
		
	}*/
	
	public static String backlashReplace(String myStr){
	    final StringBuilder result = new StringBuilder();
	    final StringCharacterIterator iterator = new StringCharacterIterator(myStr);
	    char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	     
	      if (character == '\\') {
	         result.append("/");
	      }
	       else {
	        result.append(character);
	      }

	      
	      character = iterator.next();
	    }
	    
	    result.append("/");
	    return result.toString();
	  }
}
