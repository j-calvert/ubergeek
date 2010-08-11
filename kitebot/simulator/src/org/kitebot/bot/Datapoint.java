package org.kitebot.bot;

public class Datapoint {
	long clock;
	double[] vals = new double[3];
	
	public Datapoint(long clock, double v0, double v1, double v2) {
		this.clock = clock;
		vals[0] = v0;
		vals[1] = v1;
		vals[2] = v2;
	}
}
