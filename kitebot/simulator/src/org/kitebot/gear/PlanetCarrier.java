package org.kitebot.gear;

import java.awt.Color;
import java.awt.Graphics;

public class PlanetCarrier extends Gear {
	private int x0, y0, wh, thickness;
	public PlanetCarrier(int x, int y, int rad, int thickness) {
		super(x, y, rad, 0, 0);
		x0 = x - rad;
		y0 = y - rad;
		wh = 2 * rad;
		this.thickness = thickness;
	}

	public void paint(Graphics g) {
		ann(g, color1, 180);
		ann(g, color2, 360);
	}
	
	private void ann(Graphics g, Color c, int startAngle) {
		g.setColor(c);
		g.fillArc(x0 - thickness, y0 - thickness, wh + 2 * thickness, wh + 2 * thickness, 
				startAngle - (int) (angle / (2 * Math.PI) * 360), 180);
		g.setColor(interior);
		g.fillArc(x0 + thickness, y0 + thickness, wh - 2 * thickness, wh - 2 * thickness, 
				startAngle - (int) (angle / (2 * Math.PI) * 360), 180);
		
	}
	
}