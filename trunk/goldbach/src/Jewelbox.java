import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Jewelbox implements Graphable {
	
	int n;
	Sieve primes;
	Map<Integer, Jewel> jewels = new HashMap<Integer, Jewel>();
	
	public Jewelbox(int n) {
		this.n = n;
		primes = new Sieve(2 * n);
		for(int p : primes){
			if(2 * n % p != 0){
				jewels.put(p, new Jewel(p));
			}
		}
	}
	
	public void printGraph(Writer out) throws IOException {
		out.write("digraph gb {\n");
		out.write("label = \"" + 2 * n + "\"");
		for(Jewel jewel : jewels.values()){
			jewel.printGraph(out);
		}
		out.write("}");
	}
	
	private class Jewel {
		int p;
		List<Integer> complement;
		Jewel(int p){
			this.p = p;
			complement = primes.getFactors(2 * n - p);
		}
		
		void printGraph(Writer out) throws IOException {
			for(int c : complement){
				out.write(p + " -> " + c + "\n");
			}
		}
	}
	
	

}
