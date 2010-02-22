package povgen;
import java.io.IOException;

public class PovGen extends PovBase {

	


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
		time = frame / FRAMES_PER_SECOND;
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
		camLoc.x = -700;
		camLoc.y = 740;
		camLoc.z = 120;
		camLook.x = 0;
		camLook.y = 320;
		camLook.z = 100;
	}

	private void shootFrame() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		
		CompSet.steeringFloating.print();
//		pipe("text.pov", "union {", " translate<0,.05,0>}");

		pipe("sky2.pov");

		snap(formatter.format(frame));
		System.out.println("i, rv, sv, s: " + frame + ", " + rv + ", " + sv
				+ ", " + s);
	}

}
