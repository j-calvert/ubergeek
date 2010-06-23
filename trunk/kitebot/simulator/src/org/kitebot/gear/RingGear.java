package org.kitebot.gear;

import java.awt.Graphics;

public class RingGear extends Gear {

	int pixelWidth = 50;

	public RingGear(int x, int y, int rad, int numGearTeeth, int pixelGearDepth) {
		super(x, y, rad, numGearTeeth, pixelGearDepth);
	}

	public void paint(Graphics g) {
		int[] nxX = new int[4], nxY = new int[4];
		double rGearAngle;
		double rIdx;
		double rSizeH = rad - (double) pixelGearDepth / 2;
		double rSizeL = rad + (double) pixelGearDepth / 2;
		rGearAngle = 2 * Math.PI / gearCount;

		g.setColor(color1);
		g.fillArc((int) (x - rSizeL - pixelWidth / 2),
				(int) (y - rSizeL - pixelWidth / 2),
				(int) (rSizeL * 2 + pixelWidth),
				(int) (rSizeL * 2 + pixelWidth), 180 - (int) (angle
						/ (2 * Math.PI) * 360), 180);
		g.setColor(color2);
		g.fillArc((int) (x - rSizeL - pixelWidth / 2),
				(int) (y - rSizeL - pixelWidth / 2),
				(int) (rSizeL * 2 + pixelWidth),
				(int) (rSizeL * 2 + pixelWidth), 360 - (int) (angle
						/ (2 * Math.PI) * 360), 180);

		g.setColor(interior);
		g.drawArc((int) (x - rSizeL - pixelWidth / 2),
				(int) (y - rSizeL - pixelWidth / 2),
				(int) (rSizeL * 2 + pixelWidth),
				(int) (rSizeL * 2 + pixelWidth), 0, 360);

		g.setColor(interior);
		g.fillArc((int) (x - rSizeL), (int) (y - rSizeL), (int) (rSizeL * 2),
				(int) (rSizeL * 2), 0, 360);

		g.setColor(color1);
		nxX[2] = rotateX(rSizeL, 0);
		nxY[2] = rotateY(rSizeL, 0);
		for (rIdx = 0.0; rIdx < Math.PI - 0.000001; rIdx += rGearAngle) {
			nxX[0] = nxX[2];
			nxY[0] = nxY[2];
			nxX[1] = rotateX(rSizeH, rIdx + rGearAngle / 2);
			nxY[1] = rotateY(rSizeH, rIdx + rGearAngle / 2);
			nxX[2] = rotateX(rSizeL, rIdx + rGearAngle);
			nxY[2] = rotateY(rSizeL, rIdx + rGearAngle);
			nxX[3] = rotateX(rSizeL + 4, rIdx + rGearAngle / 2);
			nxY[3] = rotateY(rSizeL + 4, rIdx + rGearAngle / 2);
			g.fillPolygon(nxX, nxY, 4);
		}

		g.setColor(color2);
		if (gearCount % 2 == 1) {
			nxX[0] = rotateX(rSizeL, rIdx - rGearAngle / 2);
			nxY[0] = rotateY(rSizeL, rIdx - rGearAngle / 2);
			g.fillPolygon(nxX, nxY, 3);
		}
		nxX[2] = rotateX(rSizeL, rIdx);
		nxY[2] = rotateY(rSizeL, rIdx);
		for (; rIdx < 2 * Math.PI - 0.000001; rIdx += rGearAngle) {
			nxX[0] = nxX[2];
			nxY[0] = nxY[2];
			nxX[1] = rotateX(rSizeH, rIdx + rGearAngle / 2);
			nxY[1] = rotateY(rSizeH, rIdx + rGearAngle / 2);
			nxX[2] = rotateX(rSizeL, rIdx + rGearAngle);
			nxY[2] = rotateY(rSizeL, rIdx + rGearAngle);
			nxX[3] = rotateX(rSizeL + 4, rIdx + rGearAngle / 2);
			nxY[3] = rotateY(rSizeL + 4, rIdx + rGearAngle / 2);
			g.fillPolygon(nxX, nxY, 4);
		}
	}
}