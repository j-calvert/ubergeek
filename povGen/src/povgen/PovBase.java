package povgen;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class PovBase {
	protected static NumberFormat formatter = new DecimalFormat("0000");
	protected static final double MAX_ANGULAR_VEL = 1;
	protected static final int SUN_PERIOD = 2;
	protected static double LEGEND_SCALE = 15;
	protected static String OUT = "out.pov";
	protected int NUM_FRAMES = 1;
	protected int FRAMES_PER_SECOND = 30;
	protected Vec camLoc = new Vec(1.7, .5, 3.3);
	protected Vec camLook = new Vec(1.7, .4, 0);
	protected double camAngle = 80;
	protected double pivotAngle = 0;
	
	protected double s = 0, r = 0;
	protected double rv = MAX_ANGULAR_VEL, sv = 0;
	protected int frame = 0;
	protected double time = 0;
	
	
	protected void newFrame() {
		 new File(OUT).delete();
	}
	
	protected static void print(String s, String f) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
		out.write(s + "\n");
		out.close();

	}

	protected void print(String s) throws IOException {
		print(s, OUT);
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

	protected void pipe(String f1, String f2) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
		BufferedWriter out = new BufferedWriter(new FileWriter(f2, true));

		String s;
		while ((s = in.readLine()) != null) {
			out.write(s + "\n");
		}
		in.close();
		out.close();
	}
	
	protected void pipe(String f1) throws IOException {
		pipe(f1, OUT);
	}

	protected void snap(String name) {
		snap(name, 640);
	}
	
	protected void snap(String name, int width) {
		if(width % 4 != 0){
			throw new RuntimeException("width not multiple of 4");
		}
		int height = width / 4 * 3;
		exec("povray +UV +UL +A0.2 +FN16 -W" + width + " -H" + height + " out.pov +O"
			+ name + ".png");
	}

	protected void snapWide(String name, int width) {
		if(width % 4 != 0){
			throw new RuntimeException("width not multiple of 4");
		}
		int height = width / 2;
		exec("povray +UV +UL +A0.2 +FN16 -W" + width + " -H" + height + " out.pov +O"
			+ name + ".png");
	}
	
	protected void exec(String cmd) {
		String s = null;
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			while ((s = stdInput.readLine()) != null) {
//				System.out.println(s);
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

	protected static class Vec {
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
		
		public Vec minus(Vec that) {
			return vec(this.x - that.x, this.y - that.y, this.z - that.z);			
		}
		
		public Vec ave(Vec that, double d) {
			return vec(this.x * (1 - d) + that.x * d, this.y * (1 - d) + that.y * d, this.z * (1 - d) + that.z * d);
		}
		public Vec scale(double d) {
			return vec(this.x * d, this.y * d, this.z * d);
		}
		
	}
	
	public static Vec vec(double f1, double f2, double f3) {
		return new Vec(f1, f2, f3);
	}

	protected String light(Vec loc, Vec color) {
		return "      light_source {" + loc + "\n"
				+ "        color rgb " +  color + " }";
	}

	protected String camera(Vec camLoc, Vec camLook, double camAngle) {
		return "      camera {  location " + camLoc + "\n"
				+ "        up < 0.000000, 1.000000, 0.000000>\n"
				+ "        right < 0.000, 0.000000, -1.0>\n"
				+ "        look_at  " + camLook + "\n"
				+ "        angle " + camAngle + " }";
	}

	
}
