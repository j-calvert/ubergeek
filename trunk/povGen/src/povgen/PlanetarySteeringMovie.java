package povgen;

import java.io.IOException;

public class PlanetarySteeringMovie extends PovBase {



	public static void main(String[] args) throws Exception {
		Ac3d.parse("render.pov");
		new PlanetarySteeringMovie().movie();

	}

	private void movie() throws IOException {
		NUM_FRAMES = 400;
		for (frame = 0; frame < NUM_FRAMES; frame++) {
			advance();
			shootPlanetary();
			shootPivot();
			String frm = formatter.format(frame);
			exec("convert pivot" + frm + ".png planetary" + frm + ".png -gravity Northwest -composite overlap" + frm + ".png");
		}
		// exec("mogrify -format jpg -quality 90 *.png");
		// new File("video.mpg").delete();
		// exec("ffmpeg -f image2 -i out%04d.jpg video.mpg");
	}

	private void advance() {
		time = frame / FRAMES_PER_SECOND;
		double period = 4;
		s = 360 * Math.sin(time / period);
		if(s> 180) {
			s = 180;
		} else if(s < -180) {
			s = -180;
		}
		pivotAngle = -30 * Math.cos(time/period);
		r = -s/6;
	}

	private void shootPlanetary() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		camLoc = vec(-45, 10, 70);
		camLook = vec(0, 0, 10);

		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		pipe("sceneIndoors.pov");

		print("union {");
		pov(Comp.SteeringSABlueChainwheel, Comp.SteeringSARedChainWheel, Comp.SteeringSAAxle);
		Vec translate = vec(0, 0, 0);
		print(" rotate <0, 180, 0> translate " + translate + "}");
		
		snap("planetary" + formatter.format(frame), 400);
		
	}
	
	private void shootPivot() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		camLoc = vec(-600, 400, -130);
		camLook = vec(0, 400, -100);		
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		pipe("sceneGrassyField.pov");

		print("union{");
		CompSet.steeringFloating.print();
		pipe("steeringWheel.pov", "union { ", Comp.SteeringWheel.texture() +" rotate<" + s + ",0,0> translate <0, 250.952, -47.5893>}");
		print(" rotate<0, " + pivotAngle + ",0>}");
		
		CompSet.anchoredSet().print();
		snap("pivot" + formatter.format(frame), 800);
	}
	
	

	
	public void pov(Comp outerComp, Comp planetComp, Comp sunComp) throws IOException {

		double ann = (3 * r + s) / 4;
		double p = ann - s;

		pipe("gearMacros.pov");
		pipe("steeringWheel.pov", "union {", sunComp.texture() + " rotate<" + s + ",0,0>\n"
				+ "	scale<.5, .5, .5> translate<-20,0,0> }");
		print("union {cylinder { <40,0,0>,<0,0,0>,4 }" + sunComp.texture() +"}" );
		pipe("outerCW.pov", "union {", outerComp.texture() + " rotate<"
				+ r + ",0,0>}\n");
		pipe("planetCW.pov", "union {", planetComp.texture()
				+ " rotate <" + ((3 * r + s) / 4) + ",0,0>\n"
				+ "	translate<-30,0,0>\n" + "}\n");

		print("union {\n" + "	object{ GearInv ( 60, 0.15, 0.5) \n"
				+ outerComp.texture() + "rotate<0,"
				+ r
				+ ",0>}\n"
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
				+ "			cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
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
				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
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
				+ "		cylinder { <0,0,0>,<0,-30/16,0>,.15 } \n"
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
}

	
