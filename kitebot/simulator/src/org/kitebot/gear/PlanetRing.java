package org.kitebot.gear;

import java.awt.Color;
import java.awt.Graphics;

public class PlanetRing {

	protected PlanetCarrier planetCarrier;
	protected Gear[] planetGears;

	public PlanetRing(int x, int y, int planetCarrierRadius,
			int numPinionGears, int planetGearRadius, int numPinionGearTeeth,
			int ringGearCount, GearColor plntColor, GearColor annColor) {
		planetGears = new Gear[numPinionGears];
		planetCarrier = new PlanetCarrier(x, y, (int) planetCarrierRadius, 6,
				annColor);

		for (int i = 0; i < planetGears.length; i++) {
			planetGears[i] = new Gear(x, y, planetGearRadius,
					numPinionGearTeeth, plntColor);

			planetGears[i].x = planetCarrier.rotateX(planetCarrierRadius,
					(2 * Math.PI) * i / planetGears.length);
			planetGears[i].y = planetCarrier.rotateY(planetCarrierRadius,
					(2 * Math.PI) * i / planetGears.length);

			planetGears[i].setAngle((2 * Math.PI) * i / planetGears.length
					+ (2 * Math.PI) / planetGears[i].gearCount / 2
					- (2 * Math.PI) / planetGears[i].gearCount
					* (ringGearCount % numPinionGears) * i / numPinionGears);
		}

	}

	public void move(double millisec, double planetCarrierSpeed,
			double ringGearSpeed, int ringGearCount) {

		double theFactor = 1d / 60 / 1000 * millisec * 2 * Math.PI;

		double pinionGearSpeed = planetCarrierSpeed
				+ (ringGearSpeed - planetCarrierSpeed) * ringGearCount
				/ planetGears[0].gearCount;

		planetCarrier.setAngle(planetCarrier.angle + planetCarrierSpeed
				* theFactor);

		for (int nIdx = 0; nIdx < planetGears.length; nIdx++) {

			planetGears[nIdx].x = planetCarrier.rotateX(planetCarrier.rad,
					(2 * Math.PI) * nIdx / planetGears.length);
			planetGears[nIdx].y = planetCarrier.rotateY(planetCarrier.rad,
					(2 * Math.PI) * nIdx / planetGears.length);

			planetGears[nIdx].setAngle(planetGears[nIdx].angle
					+ pinionGearSpeed * theFactor);
		}
	}

	public int getPlanetGearCount() {
		return planetGears[0].gearCount;
	}

	public void paint(Graphics g) {
		planetCarrier.paint(g);
		for (int nIdx = 0; nIdx < planetGears.length; nIdx++) {
			planetGears[nIdx].paint(g);
		}
	}

	private static class PlanetCarrier extends Gear {
		private int x0, y0, wh, thickness;

		public PlanetCarrier(int x, int y, int rad, int thickness,
				GearColor annColor) {
			super(x, y, rad, 0, annColor);
			x0 = x - rad;
			y0 = y - rad;
			wh = 2 * rad;
			this.thickness = thickness;
			this.gColor = annColor;
		}

		public void paint(Graphics g) {
			ann(g, gColor.fg(), 180);
			ann(g, gColor.bg(), 360);
		}

		private void ann(Graphics g, Color c, int startAngle) {
			g.setColor(c);
			g.fillArc(x0 - thickness, y0 - thickness, wh + 2 * thickness, wh
					+ 2 * thickness, startAngle
					- (int) (angle / (2 * Math.PI) * 360), 180);
			g.setColor(GearColor.INTERIOR.fg());
			g.fillArc(x0 + thickness, y0 + thickness, wh - 2 * thickness, wh
					- 2 * thickness, startAngle
					- (int) (angle / (2 * Math.PI) * 360), 180);

		}

	}

	public double getAngle() {
		return planetCarrier.getAngle();
	}

}
