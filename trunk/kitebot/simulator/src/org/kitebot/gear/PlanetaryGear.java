package org.kitebot.gear;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class PlanetaryGear extends Component {

	protected Gear ringGear;
	protected PlanetCarrier planetCarrier;
	protected Gear sunGear;
	protected Gear[] planetGears;

	public PlanetaryGear(int x, int y, int sunGearRadius, int numSunGearTeeth,
			int numPinionGearTeeth, int numRingGearTeeth, int nGearDepth,
			int numPinionGears) {

		int planetGearRadius = (numPinionGearTeeth / numSunGearTeeth)
				* sunGearRadius;
		double planetCarrierRadius = sunGearRadius + planetGearRadius;

		planetGears = new Gear[numPinionGears];

		if ((numRingGearTeeth + numSunGearTeeth) % planetGears.length != 0)
			throw new RuntimeException("gear can't contact smoothly");
		if (numRingGearTeeth <= numSunGearTeeth)
			throw new RuntimeException("less ring gear teeth than sun gear");

		int ringGearRadius = sunGearRadius + 2 * planetGearRadius;
		ringGear = new RingGear(x, y, ringGearRadius, numRingGearTeeth,
				nGearDepth);
		sunGear = new Gear(x, y, sunGearRadius, numSunGearTeeth, nGearDepth);
		planetCarrier = new PlanetCarrier(x, y, (int) planetCarrierRadius, 10);

		planetCarrier.color2 = Color.red;
		sunGear.color2 = Color.blue;
		ringGear.color2 = new Color(0, 0xbb, 0);
		Color pinionFg = Color.orange;
		Color pinonBg = Color.darkGray;
		for (int i = 0; i < planetGears.length; i++) {
			planetGears[i] = new Gear(x, y, planetGearRadius,
					numPinionGearTeeth, nGearDepth);
			
			planetGears[i].color1 = pinionFg;
			planetGears[i].color2 = pinonBg;
			planetGears[i].x = planetCarrier.rotateX(planetCarrierRadius,
					(2 * Math.PI) * i / planetGears.length);
			planetGears[i].y = planetCarrier.rotateY(planetCarrierRadius,
					(2 * Math.PI) * i / planetGears.length);

			planetGears[i].setAngle((2 * Math.PI) * i / planetGears.length
					+ (2 * Math.PI) / planetGears[i].gearCount / 2
					- (2 * Math.PI) / planetGears[i].gearCount
					* (ringGear.gearCount % numPinionGears) * i
					/ numPinionGears);
		}


		if (numPinionGearTeeth % 2 == 1)
			sunGear.setAngle(2 * Math.PI / (sunGear.gearCount * 2));
	}

	public void move(double millisec, double planetCarrierSpeed,
			double ringGearSpeed) {

		double theFactor = 1d / 60 / 1000 * millisec * 2 * Math.PI;

		double sunGearSpeed = computeSunGearSpeed(planetCarrierSpeed,
				ringGearSpeed);

		sunGear.setAngle(sunGear.angle + sunGearSpeed * theFactor);

		ringGear.setAngle(ringGear.angle + ringGearSpeed * theFactor);

		planetCarrier.setAngle(planetCarrier.angle + planetCarrierSpeed
				* theFactor);

		double pinionGearSpeed = planetCarrierSpeed
				+ (ringGearSpeed - planetCarrierSpeed) * ringGear.gearCount
				/ planetGears[0].gearCount;

		double planetCarrierSize = sunGear.rad + (ringGear.rad - sunGear.rad)
				/ 2;

		for (int nIdx = 0; nIdx < planetGears.length; nIdx++) {

			planetGears[nIdx].x = planetCarrier.rotateX(planetCarrierSize,
					(2 * Math.PI) * nIdx / planetGears.length);
			planetGears[nIdx].y = planetCarrier.rotateY(planetCarrierSize,
					(2 * Math.PI) * nIdx / planetGears.length);

			planetGears[nIdx].setAngle(planetGears[nIdx].angle
					+ pinionGearSpeed * theFactor);
		}
	}

	// sunGearSpeed + 3 * ringGearSpeed - 4 * planetCarrierSpeed = 0;
	public static double computeSunGearSpeed(double planetCarrierSpeed,
			double ringGearSpeed) {
		return -3 * ringGearSpeed + 4 * planetCarrierSpeed;
	}

	public void paint(Graphics g) {
		ringGear.paint(g);
		planetCarrier.paint(g);
		sunGear.paint(g);
		for (int nIdx = 0; nIdx < planetGears.length; nIdx++)
			planetGears[nIdx].paint(g);
	}

}