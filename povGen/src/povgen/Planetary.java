package povgen;

import java.io.IOException;

public class Planetary extends PovBase {

	public void pov(int r, double s) throws IOException {

		double ann = (3 * r + s) / 4;
		double p = ann - s;

		pipe("gearMacros.pov");
		pipe("sunCW.pov", "union {", "	cylinder { <1,0,0>,<-30,0,0>,1.5 } \n"
				+ "	texture { ac3d_col_6 }\n" + " rotate<" + s + ",0,0>\n"
				+ "	translate<30,0,0>}");
		pipe("outerCW.pov", "union {", "texture { ac3d_col_3 }\n" + " rotate<"
				+ r + ",0,0>}\n");
		pipe("planetCW.pov", "union {", "    texture { ac3d_col_9 }\n"
				+ " rotate <" + ((3 * r + s) / 4) + ",0,0>\n"
				+ "	translate<-30,0,0>\n" + "}\n");

		print("union {\n" + "	object{ GearInv ( 60, 0.15, 0.5) \n"
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
				+ "	scale <21, 21, 21>\n" + "	rotate<90,90,0>\n" + "}"
		);
	}

	
	public void legend(int rv, int sv, int s, int legend_scale) throws IOException {
		cylinder(new Vec(44,0,0),new Vec(44, ndg(rv * legend_scale),0), 2, 3);
		cylinder(new Vec(48,0,0),new Vec(48, ndg((3 * rv + sv) / 4 * legend_scale),0), 2, 9);
		cylinder(new Vec(52,0,0),new Vec(52, ndg(sv * legend_scale),0), 2, 6);
		cylinder(new Vec(60,0,0),new Vec(60, ndg(s / 500 * legend_scale),0), 2, 10);
	}
	
	private void cylinder(Vec base, Vec top, double width, int color) throws IOException {
		print("union {	cylinder { " + base + "," + top + "," + width + "} \n" + "	texture { ac3d_col_" + color + " }}\n");		
	}
	

	private double ndg(double d) {
		return d == 0 ? .000000001 : d;
	}

}
