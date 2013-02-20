import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Generate {

	public static void main(String[] args) {
		List<String> celulas = new ArrayList<String>();
		celulas.add("A");
		celulas.add("B");
		celulas.add("C");
		celulas.add("D");
		
		List<String> colunas = new ArrayList<String>();
		colunas.add("e-Shop");
		colunas.add("Buyer Agent");
		colunas.add("OLIS");
		colunas.add("OLISTTTT");
		
		List<String> linhas= new ArrayList<String>();
		linhas.add("Felipe");
		linhas.add("Heitor");
		linhas.add("Aleixo");
		linhas.add("AleixoAAA");
		
		try {
			LatinSquare ls = new  LatinSquare(linhas, colunas, celulas);
			System.out.println(ls.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//generate(linhas, colunas, celulas);
	}
	
	private static void generate(List<String> linhas, List<String> colunas, List<String> celulas){
		Collections.shuffle(celulas);//a validação do tamanho do quadrado será validada na DSL

		//gerar o quadrado básico
		String matriz[][] = new String[celulas.size()][celulas.size()];
		int indice=0;
		for (int i = 0; i < matriz.length; i++) {
			for (int j = 0; j < matriz.length; j++) {
				indice = ((i+j)%matriz.length);
				matriz[i][j] = celulas.get(indice);
			}
		}
		
		//define a ordem das colunas
		Collections.shuffle(colunas);
		
		//define a ordem das linhas
		Collections.shuffle(linhas);
		
		
		//Embaralhando o conteúdo das linhas
		List<Integer> linhasParaEmbaralhar = new ArrayList<Integer>();
		int contLinhaInicio = 0;
		for (int i=0; i < matriz.length; i++) {
			linhasParaEmbaralhar.add(i);
		}
		
		String matriz2[][] = new String[celulas.size()][celulas.size()];
		while(linhasParaEmbaralhar.size() >= 3){
			linhasParaEmbaralhar.remove(0);
			contLinhaInicio++;
			Collections.shuffle(linhasParaEmbaralhar);
			//embaralha as linhas da matriz
			
			
			//copia a matriz
			for (int i = 0; i < matriz.length; i++) {
				for (int j = 0; j < matriz.length; j++) {
					matriz2[i][j] = matriz[i][j];
				}
			}
			
			int indiceLinhaEmbaralhar = 0;
			for (int i = (matriz.length-linhasParaEmbaralhar.size()); i < matriz.length; i++) {
				for (int j = 0; j < matriz.length; j++) {
					matriz2[i][j]=matriz[linhasParaEmbaralhar.get(indiceLinhaEmbaralhar)][j];
				}
				indiceLinhaEmbaralhar++;
			}
			System.out.println(linhasParaEmbaralhar);
			/*System.out.println(contLinhaInicio);
			System.out.println(linhasParaEmbaralhar);
			System.out.println();
			*/
			
			
		};
		
		
		//imprime resultado
		for (String coluna : colunas) {
			System.out.print("| "+coluna+" |");
		}
		System.out.println();
		
		for (int i = 0; i < matriz.length; i++) {
			System.out.print(linhas.get(i)+"= ");
			for (int j = 0; j < matriz.length; j++) {
				System.out.print("["+ matriz[i][j]+"]");
			}
			System.out.println();
		}
		
		System.out.println();
		for (int i = 0; i < matriz2.length; i++) {
			System.out.print(linhas.get(i)+"= ");
			for (int j = 0; j < matriz2.length; j++) {
				System.out.print("["+ matriz2[i][j]+"]");
			}
			System.out.println();
		}
		
	}
	
	

}
