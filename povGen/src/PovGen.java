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
	
	public static double[] sunVelocities = new double[] {0, 2, 2.5, 0, -2, -2.5, 0};

	public static void main(String[] args) throws Exception {
		planetaryMovie();
//		exec("mogrify -format jpg -quality 90 *.png");
//		new File("video.mpg").delete();
//		exec("ffmpeg -f image2 -i out%04d.jpg video.mpg");
	}
	
	public static void planetaryMovie() throws IOException {
		double s = 0, r = 0;
		double rv = 2;
		for(int scene = 0; scene < sunVelocities.length - 1; scene++) {
			for(int frame = 0; frame < 50; frame++) {
				double sv = (sunVelocities[scene] * (50 - frame) + sunVelocities[scene + 1] * frame) / 50;
				if(sv > rv) {
					rv = sv;
				} else if(sv < 0 && rv > 2) {
					rv = rv - .5/100;
				}
				s = s + 360 * sv / 30;
				r = r + 360 * rv / 30;
				planetaryShot(frame + 50 * scene, r, s, rv, sv);
			}
		}
		
	}

	public static void planetaryShot(int i, double r, double s, double rv, double sv) throws IOException {
		String out = "out.pov";
		new File(out).delete();
		int cX = 88;
		int cY = 0;
		int cZ = -140;
		int kX = 0;
		int kY = 0;
		int kZ = 0;
		int a = 50;
		echoPipe(printCamera(cX, cY, cZ, kX, kY, kZ, a), out);
		pipeFile("declare.pov", out);
		pipeFile("gearMacros.pov", out);
		echoPipe(planetary(r, s), out);
		wrapPipeFile("sunCW.pov", out, "union {", "	cylinder { <1,0,0>,<-30,0,0>,1.5 } \n" + 
				"	texture { ac3d_col_6 }\n" +
				" rotate<" + s + ",0,0>\n" +
				"	translate<30,0,0>}");
		wrapPipeFile("outerCW.pov", out, "union {", "texture { ac3d_col_3 }\n" + 
								" rotate<" + r + ",0,0>}\n");
		wrapPipeFile("planetCW.pov", out, "union {", "    texture { ac3d_col_9 }\n" +
				" rotate <" + ((3 * r + s) / 4) + ",0,0>\n" +
				"	translate<-30,0,0>\n" + 
				"}\n" );
		double scale = 15;
		echoPipe("union {	cylinder { <44,0,0>,<44," + rv * scale  + ",0>,2 } \n" + 
		"	texture { ac3d_col_3 }}\n", out);
		echoPipe("union {	cylinder { <48,0,0>,<48," +  (3 * rv + sv) / 4 * scale + ",0>,2} \n" + 
				"	texture { ac3d_col_9 }}\n", out);
		echoPipe("union {	cylinder { <52,0,0>,<52," + sv * scale + ",0>,2} \n" + 
				"	texture { ac3d_col_6 }}\n", out);
		echoPipe("union {	cylinder { <60,0,0><60," + s + ",0>,2} \n" + 
				"	texture { ac3d_col_10 }}\n", out);
		echoPipe("background{color rgb <0.52734375,  0.8046875, 0.9765625>}", out);
		echoPipe(printLight(cX - 10, cY + 40, cZ + 10), out);
		exec("povray +UV +UL +A0.2 +FN16 -W640 -H480 out.pov +Oframe" + formatter.format(i) + ".png");

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

	private static void wrapPipeFile(String f1, String f2, String head, String tail) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
		BufferedWriter out = new BufferedWriter(new FileWriter(f2, true));
		out.write(head + "\n");
		String s;
		while ((s = in.readLine()) != null) {
			out.write(s + "\n");
		}
		out.write(tail + "\n");
		in.close();
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

		double a = (3 * r + s) / 4;
		double p = a - s;
		
		return "union {\n" + 
				"	object{ GearInv ( 60, 0.15, 0.5) \n" + 
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
				"		object{ Gear (20, 0.15, 0.5) }\n" + 
				"			cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + (p + 180 / 20) + ",0>\n" + 
				"		    translate<2*Gear_Radius(20,0.15),0,0>\n" + 
				"		}\n" + 
				"		union{\n" + 
				"		object{ Gear (20, 0.15, 0.5) \n" + 
				"		}\n" + 
				"		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + (p + 180 / 20) + ",0>\n" + 
				"		    translate<-2*Gear_Radius(20,0.15),0,0>\n" + 
				"		}\n" + 
				"		union{\n" + 
				"		object{ Gear (20, 0.15, 0.5) \n" + 
				"		}\n" + 
				"		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + (p + 180 / 20) + ",0>\n" + 
				"		    translate<0,0,2*Gear_Radius(20,0.15)>\n" + 
				"		}\n" + 
				"		union{\n" + 
				"		object{ Gear (20, 0.15, 0.5) \n" + 
				"		}\n" + 
				"		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n" + 
				"				    texture { ac3d_col_9 }\n" + 
				"		    rotate<0," + (p + 180 / 20) + ",0>\n" + 
				"		    translate<0,0,-2*Gear_Radius(20,0.15)>\n" + 
				"		}\n" + 
				"	    rotate<0," + a + ",0>\n" + 
				"	}\n" + 
				"	scale <21, 21, 21>\n" + 
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

}
