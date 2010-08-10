package org.kitebot.bot;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.kitebot.gear.GearColor;

public class SliderInput extends Slider {
	private static final long serialVersionUID = 1L;

	public SliderInput(int posX, int posY, int width, int height, GearColor gColor) {
		super(posX, posY, width, height, gColor);
		enableEvents(MouseEvent.MOUSE_EVENT_MASK);
		enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		setBounds(new Rectangle(posX, posY, width, height));
		setEnabled(true);
	}
	@Override
	
	protected void processMouseEvent(MouseEvent e) {
		System.out.println(e.getModifiers());
		if (e.getModifiersEx() == 1024) {
			lastY = e.getY();
		}
	}
}
