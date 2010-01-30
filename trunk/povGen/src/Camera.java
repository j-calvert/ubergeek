public class Camera {

	public static String print(float cX, float cY, float cZ,float kX, float kY, float kZ, float a) {
		return    "        location " + vec(cX, cY, cZ) + "\n"
				+ "        up < 0.000000, 1.000000, 0.000000>\n"
				+ "        right < -1.330000, 0.000000, 0.000000>\n"
				+ "        look_at  " + vec(kX, kY, kZ) + "\n"
				+ "        angle " + a;
	}
	
	public static String vec(float f1, float f2, float f3) {
		return "< " + f1 + ", " + f2 + ", " + f3 + ">";
	}

}
