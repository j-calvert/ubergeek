package povgen;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class Util {
	
	private static String OUT = "out.pov";
	
	public static void newFrame() {
		 new File(OUT).delete();
	}
	
	public static void print(String s) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(OUT, true));
		out.write(s + "\n");
		out.close();

	}

	public static void pipe(String f1, String head, String tail) throws IOException {

		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
		BufferedWriter out = new BufferedWriter(new FileWriter(OUT, true));
		out.write(head + "\n");
		String s;
		while ((s = in.readLine()) != null) {
			out.write(s + "\n");
		}
		out.write(tail + "\n");
		in.close();
		out.close();

	}

	public static void pipe(String f1) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
		BufferedWriter out = new BufferedWriter(new FileWriter(OUT, true));

		String s;
		while ((s = in.readLine()) != null) {
			out.write(s + "\n");
		}
		in.close();
		out.close();
	}
	
	public static void exec(String cmd) {

		String s = null;

		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}	

	public static class Vec {
		double x = 0;
		double y = 0;
		double z = 0;
		
		public Vec(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		@Override
		public String toString() {
			return "< " + x + ", " + y + ", " + z + ">";
			
		}
		
	}
	
	public static Vec vec(double f1, double f2, double f3) {
		return new Vec(f1, f2, f2);
	}

}
