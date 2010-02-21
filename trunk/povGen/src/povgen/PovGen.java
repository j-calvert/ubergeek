package povgen;
import static povgen.Util.exec;
import static povgen.Util.newFrame;
import static povgen.Util.pipe;
import static povgen.Util.print;
import static povgen.Util.vec;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import povgen.Util.Vec;

public class PovGen {

	private static NumberFormat formatter = new DecimalFormat("0000");
	private static final double MAX_ANGULAR_VEL = 1;
	private static final int SUN_PERIOD = 2;
	private static double LEGEND_SCALE = 15;
	private int NUM_FRAMES = 1;
	private int SECONDS_PER_FRAME = 30;
	private Vec camLoc = new Vec(1.7, .5, 3.3);
	private Vec camLook = new Vec(1.7, .4, 0);
	private double camAngle = 80;
	private double s = 0, r = 0;
	private double rv = MAX_ANGULAR_VEL, sv = 0;
	private int frame = 0;
	private double time = 0;
	


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
//		camLoc.x = 1000 * Math.sin(Math.PI / 8 * frame);
//		camLoc.z = 1000 * Math.cos(Math.PI / 8 * frame);
	}

	private void shootFrame() throws IOException {
		newFrame();
		// lights
		print(light(vec(1.7, 2500, 2500), vec(1,1,1)));
//		print(light(camLoc, vec(.2, .2, .2)));
		// camera
		print(camera());
		// constants
		pipe("declare.pov");
		// action
		
		pipe("text.pov", "union {", " translate<0,.05,0>}");
//		kitebot();
//		planetaryGears();
//		legend();

		// backdrop
		// outdoors
		pipe("sky2.pov");
		// indoors
		// echoPipe("background{color rgb <0.52734375,  0.8046875, 0.9765625>}");

		exec("povray +UV +UL +A0.2 +FN16 -W758 -H" + (480 * 758 / 640) + " out.pov +Oframe"
				+ formatter.format(frame) + ".png");
		System.out.println("i, rv, sv, s: " + frame + ", " + rv + ", " + sv
				+ ", " + s);
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
}
