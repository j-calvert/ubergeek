package povgen;

import java.io.IOException;

public class Planetary extends PovBase {



	public static void main(String[] args) throws Exception {
		new Planetary().movie();

	}

	private void movie() throws IOException {
		NUM_FRAMES = 1;
		camLoc = vec(-45, 120, -620);
		camLook = vec(0, 110, -700);
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
		if (3 * sv >= rv) {
			rv = 3 * sv;
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
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		pipe("sceneIndoors.pov");

		print("union {");
		pov(Comp.Flywheel, Comp.Generator, Comp.DRSAAxle);
		legend(15, Comp.Flywheel, Comp.Generator, Comp.DRSAAxle);
		Vec translate = vec(0, 111.52, -697.795);
		print(" rotate <0, 180, 0> translate " + translate + "}");
		
//		Comp.DRSAAxle.print();
//		Comp.DRSACasing.print();
//		Comp.DRSAPlanetNub.print();
//		Comp.RecoilSAChainwheel.print();
		
		snap("planetary" + formatter.format(frame));
		System.out.println("i, rv, sv, s: " + frame + ", " + rv + ", " + sv
				+ ", " + s);
	}

	
	public void pov(Comp outerComp, Comp planetComp, Comp sunComp) throws IOException {

		double ann = (3 * r + s) / 4;
		double p = ann - s;

		pipe("gearMacros.pov");
//		pipe("sunCW.pov", "union {", "	cylinder { <1,0,0>,<-30,0,0>,1.5 } \n"
//				+ sunComp.texture() + " rotate<" + s + ",0,0>\n"
//				+ "	translate<30,0,0>}");
//		pipe("outerCW.pov", "union {", outerComp.texture() + " rotate<"
//				+ r + ",0,0>}\n");
//		pipe("planetCW.pov", "union {", planetComp.texture()
//				+ " rotate <" + ((3 * r + s) / 4) + ",0,0>\n"
//				+ "	translate<-30,0,0>\n" + "}\n");

		print("union {\n" + "	object{ GearInv ( 60, 0.15, 0.5) \n"
				+ outerComp.texture() + "	        rotate<0,"
				+ r
				+ ",0>\n"
				+ "	}\n"
				+ "	\n"
				+ "	object{ Gear ( 20, 0.15, 0.5) \n"
				+ sunComp.texture()
				+ "	        rotate<0,"
				+ s
				+ ",0>\n"
				+ "	}\n"
				+ "	\n"
				+ "	union{\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) }\n"
//				+ "			cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ planetComp.texture()
				+ planetComp.texture()
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<2*Gear_Radius(20,0.15),0,0>\n"
				+ "		}\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) \n"
				+ "		}\n"
//				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ planetComp.texture()
				+ planetComp.texture()
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<-2*Gear_Radius(20,0.15),0,0>\n"
				+ "		}\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) \n"
				+ "		}\n"
//				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ planetComp.texture()
				+ planetComp.texture()
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<0,0,2*Gear_Radius(20,0.15)>\n"
				+ "		}\n"
				+ "		union{\n"
				+ "		object{ Gear (20, 0.15, 0.5) \n"
				+ "		}\n"
//				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
				+ planetComp.texture()
				+ "		    rotate<0,"
				+ (p + 180 / 20)
				+ ",0>\n"
				+ "		    translate<0,0,-2*Gear_Radius(20,0.15)>\n"
				+ "		}\n"
				+ "	    rotate<0,"
				+ ann
				+ ",0>\n"
				+ "	}\n"
				+ "	scale <21, 21, 21>\n" + "	rotate<90,90,0>\n" + "}"
		);
	}

	
	public void legend(int legend_scale, Comp outer, Comp planet, Comp sun) throws IOException {
		cylinder(new Vec(44,0,0),new Vec(44, ndg(rv * legend_scale),0), 2, outer);
		cylinder(new Vec(48,0,0),new Vec(48, ndg((3 * rv + sv) / 4 * legend_scale),0), 2, planet);
		cylinder(new Vec(52,0,0),new Vec(52, ndg(sv * legend_scale),0), 2, sun);
		cylinder(new Vec(60,0,0),new Vec(60, 0, ndg(s / 500 * legend_scale)), 2, sun);
	}
	
	private void cylinder(Vec base, Vec top, double width, Comp c) throws IOException {
		print("union {	cylinder { " + base + "," + top + "," + width + "} \n" + c.texture() + "}\n");		
	}
	

	private double ndg(double d) {
		return d == 0 ? .000000001 : d;
	}

}
