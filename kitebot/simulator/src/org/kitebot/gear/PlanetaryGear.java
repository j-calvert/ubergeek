package org.kitebot.gear;

import java.awt.Component;
import java.awt.Graphics;

import org.kitebot.Const;

public class PlanetaryGear extends Component {

	protected Gear ringGear;
	protected Gear sunGear;
	protected PlanetRing planetRing;

	public PlanetaryGear(int x, int y, int sunGearRadius, int numSunGearTeeth,
			int numPinionGearTeeth, int numRingGearTeeth, int nGearDepth,
			int numPinionGears) {

		if ((numRingGearTeeth + numSunGearTeeth) % numPinionGears != 0)
			throw new RuntimeException("gear can't contact smoothly");
		if (numRingGearTeeth <= numSunGearTeeth)
			throw new RuntimeException("less ring gear teeth than sun gear");

		int planetGearRadius = (numPinionGearTeeth / numSunGearTeeth)
		* sunGearRadius;

		ringGear = new OuterRing(x, y, sunGearRadius + 2 * planetGearRadius, numRingGearTeeth, GearColor.OUTER_RING);

		sunGear = new Gear(x, y, sunGearRadius, numSunGearTeeth, GearColor.SUN);

		if (numPinionGearTeeth % 2 == 1) {
			sunGear.setAngle(2 * Math.PI / (sunGear.gearCount * 2));
		}

		
		planetRing = new PlanetRing(x, y, sunGearRadius + planetGearRadius, numPinionGears,
				planetGearRadius, numPinionGearTeeth,
				ringGear.gearCount, GearColor.PLANETS, GearColor.ANNULUS);
	}

	public void move(double millisec, double planetCarrierSpeed,
			double ringGearSpeed) {

		double theFactor = 1d / 60 / 1000 * millisec * 2 * Math.PI;

		double sunGearSpeed = Const.planetaryRelation(planetCarrierSpeed, ringGearSpeed);

		sunGear.setAngle(sunGear.angle + sunGearSpeed * theFactor);

		ringGear.setAngle(ringGear.angle + ringGearSpeed * theFactor);

		planetRing.move(millisec, planetCarrierSpeed, ringGearSpeed, ringGear.gearCount);
	}

	public void paint(Graphics g) {
		ringGear.paint(g);
		planetRing.paint(g);
		sunGear.paint(g);
	}

}