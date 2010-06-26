package org.kitebot.gear;

import java.awt.Color;

public enum GearColor {
	SUN(Color.green, Color.white),
	OUTER_RING(Color.blue, Color.white),
	PLANETS(Color.ORANGE, Color.white),
	ANNULUS(Color.cyan, Color.white),
	INTERIOR(Color.LIGHT_GRAY, Color.white);
	
	private Color dftFgClr = Color.cyan, dftBgClr = Color.white;
	public Color fgClr = Color.cyan, bgClr = Color.white;

	GearColor(Color fgClr, Color bColor) {
		this.dftFgClr = fgClr;
		this.dftBgClr = bColor;
		this.fgClr = fgClr;
		this.bgClr = bColor;
	}

	public static void setGearState(GearState state) {
		initColors();
		switch (state) {
		case DRIVE:
			SUN.fgClr = Color.cyan;
			break;
		case ACCELERATE:
			break;
		case BRAKE:
			break;
		case RECOIL:
			break;
		}
	}

	private static void initColors() {
		for (GearColor gc : GearColor.values()) {
			gc.fgClr = gc.dftFgClr;
			gc.bgClr = gc.dftBgClr;
		}
	}
}
