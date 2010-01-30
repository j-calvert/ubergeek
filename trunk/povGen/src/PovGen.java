import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PovGen {

	private static NumberFormat formatter = new DecimalFormat("0000"); 
	public float[][] offsets = new float[][] { new float[] { 100, 100, 100 } };
	public float[][] features = new float[][] { new float[] { 0, 375, 225 },
			new float[] { 0, 250, -50 } };

	public static void main(String[] args) throws Exception {
		for(int i = 0; i < 20; i++) {
			planetaryShot(i, 20 * i, 10 * i);
		}
		exec("cd wrk; mogrify -format jpg -quality 90 *.png");
		new File("wrk/video.mpg").delete();
		exec("cd wrk; ffmpeg -f image2 -i out%d.jpg video.mpg");
	}

	public static void planetaryShot(int i, double r, double s) throws IOException {
		String out = "wrk/out.pov";
		new File(out).delete();
		int cX = 70;
		int cY = 100;
		int cZ = -100;
		int kX = 0;
		int kY = 0;
		int kZ = 0;
		int a = 50;
		echoPipe(printCamera(cX, cY, cZ, kX, kY, kZ, a), out);
		pipeFile("declare.pov", out);
		pipeFile("gearMacros.pov", out);
		echoPipe(planetary(r, s), out);
//		pipeFile("gearAssembly.pov", out);
		pipeFile("sunCW.pov", out);
		pipeFile("outerCW.pov", out);
		pipeFile("planetCW.pov", out);
//		pipeFile("sky2.pov", out);
		echoPipe("background{color rgb <0.52734375,  0.8046875, 0.9765625>}", out);
		echoPipe(printLight(cX - 10, cY + 40, cZ + 10), out);
		exec("povray +UV +UL +A0.2 +FN16 -W640 -H480 wrk/out.pov +Owrk/frame" + formatter.format(i) + ".png");

	}

	public static void kitebotShot() throws IOException {
		String out = "out.pov";
		new File(out).delete();
		int cX = -200;
		int cY = 330;
		int cZ = 145;
		int kX = 0;
		int kY = 280;
		int kZ = 95;
		int a = 50;
		echoPipe(printCamera(cX, cY, cZ, kX, kY, kZ, a), out);
		pipeFile("declare.pov", out);
		echoPipe("union { ", out);
		pipeFile("thing.pov", out);
		echoPipe("} ", out);
		pipeFile("sky2.pov", out);
		echoPipe(printLight(1500, 2500, -2500), out);
		echoPipe(printDimLight(cX, cY, cZ), out);
	}

	private static void echoPipe(String s, String f2) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f2, true));
		out.write(s + "\n");
		out.close();

	}

	private static void pipeFile(String f1, String f2) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
		BufferedWriter out = new BufferedWriter(new FileWriter(f2, true));

		String s;
		while ((s = in.readLine()) != null) {
			out.write(s + "\n");
		}
		in.close();
		out.close();
	}

	public static String printLight(double x, double y, double z) {
		return "      light_source {" + vec(x, y, z) + "\n"
				+ "        color rgb <1, 1, 1> }";
	}

	public static String printDimLight(double x, double y, double z) {
		return "      light_source {" + vec(x, y, z) + "\n"
				+ "        color rgb <.21, .21, .21> }";
	}

	public static String printCamera(double cX, double cY, double cZ,
			double kX, double kY, double kZ, double a) {
		return "      camera {  location " + vec(cX, cY, cZ) + "\n"
				+ "        up < 0.000000, 1.000000, 0.000000>\n"
				+ "        right < -1.330000, 0.000000, 0.000000>\n"
				+ "        look_at  " + vec(kX, kY, kZ) + "\n"
				+ "        angle " + a + " }";
	}

	public static String printRotate(double x, double y, double z) {
		return "  rotate " + vec(x, y, z) + "\n";
	}

	public static String printScale(double x, double y, double z) {
		return "  scale " + vec(x, y, z) + "\n";
	}

	public static String vec(double f1, double f2, double f3) {
		return "< " + f1 + ", " + f2 + ", " + f3 + ">";
	}

	public static String planetary(double r, double s) {

		double a = (3 * r - s) / 4;
		double p = 30 * (s - a) / 20;
		
		return "union {\n" + 
				"	object{ GearInv ( 80, 0.15, 0.5) \n" + 
				"	        texture { ac3d_col_3 }\n" + 
				"	        rotate<0," + r + ",0>\n" + 
				"	}\n" + 
				"	\n" + 
				"	object{ Gear ( 20, 0.15, 0.5) \n" + 
				"	        texture { ac3d_col_6 }\n" + 
				"	        rotate<0," + s + ",0>\n" + 
				"	}\n" + 
				"	\n" + 
				"	union{\n" + 
				"		union{\n" + 
				"		object{ Gear (30, 0.15, 0.45) }\n" + 
				"			cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + p + ",0>\n" + 
				"		    translate<Gear_Radius(20,0.15)+Gear_Radius(30,0.15),0,0.025>\n" + 
				"		}\n" + 
				"		union{\n" + 
				"		object{ Gear (30, 0.15, 0.45) \n" + 
				"		}\n" + 
				"		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + p + ",0>\n" + 
				"		    translate<-Gear_Radius(20,0.15)-Gear_Radius(30,0.15),0,0.025>\n" + 
				"		}\n" + 
				"		union{\n" + 
				"		object{ Gear (30, 0.15, 0.45) \n" + 
				"		}\n" + 
				"		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + p + ",0>\n" + 
				"		    translate<0,0,Gear_Radius(20,0.15)+Gear_Radius(30,0.15)>\n" + 
				"		}\n" + 
				"		union{\n" + 
				"		object{ Gear (30, 0.15, 0.45) \n" + 
				"		}\n" + 
				"		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + p + ",0>\n" + 
				"		    translate<0,0,-Gear_Radius(20,0.15)-Gear_Radius(30,0.15)>\n" + 
				"		}\n" + 
				"	    rotate<0," + a + ",0>\n" + 
				"	}\n" + 
				"	scale <16,16,16>\n" + 
				"	rotate<90,90,0>\n" + 
				"}";
	}

	public static void exec(String cmd) {

		String s = null;

		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			System.out
					.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
