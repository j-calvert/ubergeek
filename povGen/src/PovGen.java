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
	private static String OUT = "out.pov";
	private static final double MAX_ANGULAR_VEL = 1;
	private static final int SUN_PERIOD = 2;
	private static double LEGEND_SCALE = 15;
	private int NUM_FRAMES = 9;
	private int SECONDS_PER_FRAME = 30;
	private Vec camLoc = new Vec(-200, 30, 145);
	private Vec camLook = new Vec(0, 0, 0);
	private double camAngle = 50;
	private double s = 0, r = 0;
	private double rv = MAX_ANGULAR_VEL, sv = 0;
	private int frame = 0;
	private double time = 0;
	
	private static class Vec {
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
	
	private static Vec vec(double f1, double f2, double f3) {
		return new Vec(f1, f2, f2);
	}


	public static void main(String[] args) throws Exception {
		new PovGen().movie();
		// kitebotShot();
		// planetaryMovie();

	}

	private void movie() throws IOException {

		for (frame = 0; frame < NUM_FRAMES; frame++) {
			advance();
			shootFrame();
		}
		// exec("mogrify -format jpg -quality 90 *.png");
		// new File("video.mpg").delete();
		// exec("ffmpeg -f image2 -i out%04d.jpg video.mpg");
	}

	private void advance() {
		time = frame / SECONDS_PER_FRAME;
		sv = MAX_ANGULAR_VEL * Math.sin(Math.PI / 2 / SUN_PERIOD * time);
		if (sv >= rv) {
			rv = sv;
		} else if (sv < 0 && rv > MAX_ANGULAR_VEL * .6) {
			rv = rv - .5 / 10000;
		}
		s = s + 360 * sv / 60;
		r = r + 360 * rv / 60;
		// cY = 5 * frame;
		// cZ = -100 * frame + 500;
		camLoc.x = 1000 * Math.sin(Math.PI / 8 * frame);
		camLoc.z = 1000 * Math.cos(Math.PI / 8 * frame);
	}

	private void shootFrame() throws IOException {
		new File(OUT).delete();
		// lights
		print(light(vec(1500, 2500, 2500), vec(1,1,1)));
		print(light(camLoc, vec(.2, .2, .2)));
		// camera
		print(camera());
		// constants
		pipe("declare.pov");
		// action
		kitebot();
		planetaryGears();
		legend();

		// backdrop
		// outdoors
		pipe("sky2.pov");
		// indoors
		// echoPipe("background{color rgb <0.52734375,  0.8046875, 0.9765625>}");

		exec("povray +UV +UL +A0.2 +FN16 -W640 -H480 out.pov +Oframe"
				+ formatter.format(frame) + ".png");
		System.out.println("i, rv, sv, s: " + frame + ", " + rv + ", " + sv
				+ ", " + s);
	}

	private void cogHub() throws IOException {
		pipe("cogHub.pov", "union {", "texture { ac3d_col_3 }\n" + " rotate<"
				+ s + ",118.876,-493.656>}\n");
	}

	private void planetaryGears() throws IOException {
		pipe("gearMacros.pov");
		print(planetary());
		pipe("sunCW.pov", "union {", "	cylinder { <1,0,0>,<-30,0,0>,1.5 } \n"
				+ "	texture { ac3d_col_6 }\n" + " rotate<" + s + ",0,0>\n"
				+ "	translate<30,0,0>}");
		pipe("outerCW.pov", "union {", "texture { ac3d_col_3 }\n" + " rotate<"
				+ r + ",0,0>}\n");
		pipe("planetCW.pov", "union {", "    texture { ac3d_col_9 }\n"
				+ " rotate <" + ((3 * r + s) / 4) + ",0,0>\n"
				+ "	translate<-30,0,0>\n" + "}\n");
	}

	private void legend() throws IOException {
		cylinder(new Vec(44,0,0),new Vec(44, ndg(rv * LEGEND_SCALE),0), 2, 3);
		cylinder(new Vec(48,0,0),new Vec(48, ndg((3 * rv + sv) / 4 * LEGEND_SCALE),0), 2, 9);
		cylinder(new Vec(52,0,0),new Vec(52, ndg(sv * LEGEND_SCALE),0), 2, 6);
		cylinder(new Vec(60,0,0),new Vec(60, ndg(s / 500 * LEGEND_SCALE),0), 2, 10);
	}
	
	public void cylinder(Vec base, Vec top, double width, int color) throws IOException {
		print("union {	cylinder { " + base + "," + top + "," + width + "} \n" + "	texture { ac3d_col_" + color + " }}\n");		
	}

	private double ndg(double d) {
		return d == 0 ? .000000001 : d;
	}

	private void kitebot() throws IOException {
		pipe("complete.pov", "union { ", "} ");
	}

	private void print(String s) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(OUT, true));
		out.write(s + "\n");
		out.close();

	}

	private void pipe(String f1, String head, String tail) throws IOException {

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

	private void pipe(String f1) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
		BufferedWriter out = new BufferedWriter(new FileWriter(OUT, true));

		String s;
		while ((s = in.readLine()) != null) {
			out.write(s + "\n");
		}
		in.close();
		out.close();
	}

	private String light(Vec loc, Vec color) {
		return "      light_source {" + loc + "\n"
				+ "        color rgb " +  color + " }";
	}

	private String camera() {
		return "      camera {  location " + camLoc + "\n"
				+ "        up < 0.000000, 1.000000, 0.000000>\n"
				+ "        right < 0.000, 0.000000, 1.0>\n"
				+ "        look_at  " + camLook + "\n"
				+ "        angle " + camAngle + " }";
	}


	private String planetary() {

		double ann = (3 * r + s) / 4;
		double p = ann - s;

		return "union {\n" + "	object{ GearInv ( 60, 0.15, 0.5) \n"
				+ "	        texture { ac3d_col_3 }\n" + "	        rotate<0,"
				+ r
				+ ",0>\n"
				+ "	}\n"
				+ "	\n"
				+ "	object{ Gear ( 20, 0.15, 0.5) \n"
				+ "	        texture { ac3d_col_6 }\n"
				+ "	        rotate<0,"
				+ s
				+ ",0>\n"
				+ "	}\n"
				+ "	\n"
				+ "	union{\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) }\n"
				+ "			cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ "				    texture { ac3d_col_9 }\n"
				+ "		    texture { ac3d_col_9 }\n"
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<2*Gear_Radius(20,0.15),0,0>\n"
				+ "		}\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) \n"
				+ "		}\n"
				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ "				    texture { ac3d_col_9 }\n"
				+ "		    texture { ac3d_col_9 }\n"
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<-2*Gear_Radius(20,0.15),0,0>\n"
				+ "		}\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) \n"
				+ "		}\n"
				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ "				    texture { ac3d_col_9 }\n"
				+ "		    texture { ac3d_col_9 }\n"
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<0,0,2*Gear_Radius(20,0.15)>\n"
				+ "		}\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) \n"
				+ "		}\n"
				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ "				    texture { ac3d_col_9 }\n"
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<0,0,-2*Gear_Radius(20,0.15)>\n"
				+ "		}\n"
				+ "	    rotate<0,"
				+ ann
				+ ",0>\n"
				+ "	}\n"
				+ "	scale <21, 21, 21>\n" + "	rotate<90,90,0>\n" + "}";
	}

	private void exec(String cmd) {

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
