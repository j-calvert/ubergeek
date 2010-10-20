
public class Goldbach {

	private Grapher grapher = new Grapher();
	
	public static void main(String[] args) throws Exception{
		Goldbach gb = new Goldbach();
		for(int n = 10; n < 3000; n += 100) {
			Jewelbox jb = new Jewelbox(n);
			System.err.println("Displaying");
			gb.grapher.display(jb);
		}
		
	}
	
}
