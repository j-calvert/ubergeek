package org.kitebot.gear;

import java.awt.Color;

public enum GearColor {
	OUTER_RING(Color.blue),
	ANNULUS(Color.green),
	SUN(Color.cyan),
	PLANETS(Color.ORANGE),
	INTERIOR(Color.LIGHT_GRAY),
	BRAKE(Color.darkGray),
	LINE(Color.red);
	
	public Color fgClr = Color.cyan;

	GearColor(Color fgClr) {
		this.fgClr = fgClr;
	}
	
	public Color fg() {
		return fgClr;
	}
	
	public Color bg() {
		return fgClr.darker().darker().darker();
	}

}
