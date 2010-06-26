package org.kitebot;

public class Const {

	public final static int k = 3;
	public final static int Ge2s = k + 1;
	public final static int Gr2s = -k;

	public final static double jg = 1;
	public final static double je = 1;
	public final static double jm = 100;
	
	public static double planetaryRelation(double planetCarrier,
			double ringGear) {
		return Ge2s * planetCarrier + Gr2s * ringGear;
	}

	// Display
	public final static int gearDepth = 12;

}
