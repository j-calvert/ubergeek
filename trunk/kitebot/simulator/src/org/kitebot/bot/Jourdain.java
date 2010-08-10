package org.kitebot.bot;

import org.kitebot.Const;
import org.kitebot.gear.GearColor;
import org.kitebot.gear.GearState;

/**
 * Computes acceleration on components of planetary gear drive, using notation
 * in http://www.google.com/search?q=hybrid+electric+vehicle+propulsion+system+
 * architectures+of+the+e-cvt+type
 * 
 * The relations in this paper are based on Jourdain's principle, which states
 * that "the internal virtual power produced by the resulting constraint forces
 * and torques in a system of N rigid bodies is equal to zero."
 * http://eom.springer.de/v/v096250.htm#v096250_00m6
 * 
 * @author jeremyc
 */
public class Jourdain {

	private final static double Ge2s = Const.Ge2s;
	private final static double Gr2s = Const.Gr2s;

	private final static double jg = Const.jg;
	private final static double je = Const.je;
	private final static double jm = Const.jm;
	private final static double Jg = jg;
	private final static double Je = je + Ge2s * Ge2s * Jg;
	private final static double Jm = jm + Gr2s * Gr2s * Jg;

	private static double Jgc() {
		return Ge2s * Gr2s * Jg;
	}

	private static double Jeq() {
		return Je + Ge2s * Ge2s * Jg;
	}

	private static double Jmq() {
		return Jm + Gr2s * Gr2s * Jg;
	}

	// Define w, x, y, z, fa, and fb as defined by
	// (4) and (5)...where e = /omega_e' and m = /omega_m'
	// [ w x ] [ e ] [ fa ]
	// [ y z ] [ m ] = [ fb ]

	private static double w() {
		return Jeq();
	}

	private static double x() {
		return Jgc();
	}

	private static double y() {
		return Jgc();
	}

	private static double z() {
		return Jmq();
	}

	private static double fa(double me, double mg) {
		return me + Ge2s * mg;
	}

	private static double fb(double mg) {
		return Gr2s * mg;
	}

	private static double det(double w, double x, double y, double z) {
		return w * z - x * y;
	}

	private static double[] ePmP(double w, double x, double y, double z,
			double fa, double fb) {
		double det = det(w, x, y, z);
		return new double[] { (z * fa - x * fb) / det,
				(-1 * y * fa + w * fb) / det };
	}


	private static double[] acc(double d, double e, double m, double me,
			double mg, boolean brake) {
		double[] ePmP = ePmP(w(), x(), y(), z(), fa(me, mg), fb(mg));
		double s = Const.planetaryRelation(e + ePmP[0] * d, m + ePmP[1] * d);
		if (Const.CR * s > e + ePmP[0] * d) {
			GearColor.setGearState(GearState.DRIVE);
			ePmP = ePmP(w2(), x2(), y2(), z2(), fa2(me, mg), fb2());
		} else {
			if(brake) {
				GearColor.setGearState(GearState.BRAKE);
			} else if(s < 0) {
				GearColor.setGearState(GearState.RECOIL);
			}
		}
		ePmP[0] = e + ePmP[0] * d;
		ePmP[1] = m + ePmP[1] * d;
		return ePmP;
	}

	/**
	 * 
	 * @param millisec
	 * @param speedPlanetCarrier
	 * @param speedRingGear
	 * @param torquePlanetCarrier
	 * @param torqueSunGear
	 * @param brake 
	 * @return double[] {PlanetCarrierSpeed, RingGearSpeed}
	 */
	public static double[] accelerate(double delta, double speedPlanetCarrier,
			double speedRingGear, double torquePlanetCarrier,
			double torqueSunGear, boolean brake) {
		return acc(delta, speedPlanetCarrier, speedRingGear,
				torquePlanetCarrier, torqueSunGear, brake);
	}

	private static double w2() {
		return je + jg / Const.CR;
	}

	private static double x2() {
		return jm;
	}

	private static double y2() {
		return Ge2s - 1 / Const.CR;
	}

	private static double z2() {
		return Gr2s;
	}

	private static double fa2(double me, double mg) {
		return me + mg;
	}

	private static double fb2() {
		return 0;
	}
}
