package org.kitebot.gear;

import java.awt.Color;

public enum GearColor {
	OUTER_RING(Color.blue, Color.white),
	ANNULUS(Color.cyan, Color.white),
	SUN(Color.green, Color.white),
	PLANETS(Color.ORANGE, Color.white),
	INTERIOR(Color.LIGHT_GRAY, Color.white),
	BRAKE(Color.darkGray, Color.white);
	
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
			SUN.bgClr = Color.cyan;
			break;
		case BRAKE:
			SUN.bgClr = Color.DARK_GRAY;
			break;
		case RECOIL:
			SUN.bgClr = Color.blue;
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
