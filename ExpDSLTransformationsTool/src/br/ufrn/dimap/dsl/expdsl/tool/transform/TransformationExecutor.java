package br.ufrn.dimap.dsl.expdsl.tool.transform;


public class TransformationExecutor {
	public static void main(String[] args) {
		if(args[0].equals("qvto")){
			QVTOTransformationExecutor executor = new QVTOTransformationExecutor(args[1]);
			try {
				executor.execute(args[2], args[3]);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
