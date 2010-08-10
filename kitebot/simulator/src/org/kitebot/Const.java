package org.kitebot;

public class Const {

	// Gearing //
	public final static int k = 3;
	public final static int Ge2s = k + 1;
	public final static int Gr2s = -k;

	// Ratio of sun to planet ring when locked
	public static double CR = 5;


	// returns sun gear position/velocity
	public static double planetaryRelation(double planetCarrier, double ringGear) {
		return Ge2s * planetCarrier + Gr2s * ringGear;
	}

	// Masses //
	public final static double jg = 1;
	public final static double je = 1;
	public final static double jm = 100;

	// Display //
	public final static int gearDepth = 12;

}
