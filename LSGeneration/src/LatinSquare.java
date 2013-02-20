import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LatinSquare{
	private List<Tuple> tuples;
	private List<String> lines;
	private List<String> columns;
	private List<String> cells;
	
	public LatinSquare(List<String> lines, List<String> columns, List<String> cells) throws Exception {
		validate(lines, columns, cells);
		this.lines = lines;
		this.columns = columns;
		this.cells = cells;
		shuffleData();
		loadTuples();
		shuffleTuples();
	}

	private void shuffleTuples() {
		for (int i = 1; i < (this.tuples.size()-1); i++) {
			List<Tuple> subList = this.tuples.subList(i, this.tuples.size());
			Collections.shuffle(subList);
			for (int j = 0; j < subList.size(); j++) {
				this.tuples.set(i+j, subList.get(j));
			}
		}
	}

	private void loadTuples() {
		this.tuples = new ArrayList<Tuple>();
		for (int i = 0; i < this.lines.size(); i++) {
			this.tuples.add(new Tuple(this.cells, i));
		}
	}

	private void shuffleData() {
		Collections.shuffle(this.lines);
		Collections.shuffle(this.columns);
		Collections.shuffle(this.cells);
	}

	private void validate(List<String> lines, List<String> columns, List<String> cells) throws Exception {
		if(cells.size() != columns.size() || 
				cells.size() != lines.size()){
			throw new Exception("Latin Square invalid.");
		}
	}
	
	/* TO-DO se for necessário
	public String[][] getMatrix(){
		
	}
	*/
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		s.append("\t");
		for (String collumn : this.columns) {
			s.append(collumn + "|");
		}
		s.append("\n");
		for (String line : this.lines) {
			s.append(line + "|");
			s.append("\n");
		}
		
		for (Tuple tuple : this.tuples) {
			for (String cell : tuple.getCells()) {
				s.append(cell + "|");				
			}
			s.append("\n");
		}
		
		return s.toString();
	}
	
	class Tuple{
		private List<String> cells;
		public Tuple(List<String> cells, int i) {
			this.cells = new ArrayList<String>(cells);
			moveCalls(i);
		}
		private void moveCalls(int i) {
			for (int j = 0; j < i; j++) {
				this.cells.add(this.cells.remove(0));
			}
		}
		public List<String> getCells() {
			return cells;
		}
	}
}