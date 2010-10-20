import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class Grapher {

	private static File tmpDir = new File("/tmp");
	
	public void display(Graphable graphable) throws IOException, InterruptedException{
		Random random = new Random();
		File tf = new File(tmpDir, random.nextLong() + ".dot");
		File pf = new File(tmpDir, random.nextLong() + ".png");
		BufferedWriter out = new BufferedWriter(new FileWriter(tf));
		graphable.printGraph(out);
		out.close();
		String command = "dot -Tpng -o " + pf.getAbsolutePath() + " " + tf.getAbsolutePath();
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		p = Runtime.getRuntime().exec("display " + pf);
		p.waitFor();
		tf.delete();
		pf.delete();
	}
	
}
