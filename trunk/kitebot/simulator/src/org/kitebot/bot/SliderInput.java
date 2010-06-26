package org.kitebot.bot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public class SliderInput extends Component {
	private static final long serialVersionUID = 1L;

	private int width, height, posX, posY, lastY;

	Color bgColor = Color.black;
	Color fgColor = Color.blue;

	public SliderInput(int posX, int posY, int width, int height) {
		enableEvents(MouseEvent.MOUSE_EVENT_MASK);
		enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
		this.lastY = height;
		setBounds(new Rectangle(posX, posY, width, height));
		setEnabled(true);
	}

	public void paint(Graphics g) {
		g.setColor(bgColor);
		g.fillRect(posX, posY, width, lastY);
		g.setColor(fgColor);
		g.fillRect(posX, posY + lastY, width, height - lastY);
	}

	// public void processEvent(AWTEvent e) {
	// MouseEvent event = (MouseEvent) e;
	// lastY = event.getY();
	// lastY = lastY < 0 ? 0 : lastY;
	// lastY = lastY > height ? height : lastY;
	// }

	@Override
	protected void processMouseEvent(MouseEvent e) {
		System.out.println(e.getModifiers());
		if (e.getModifiersEx() == 1024) {
			lastY = e.getY();
			lastY = lastY < 0 ? 0 : lastY;
			lastY = lastY > height ? height : lastY;
		}
	}

	public double getForceFraction() {
		return 1d * (height - lastY) / height;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void setFgColor(Color fgColor) {
		this.fgColor = fgColor;
	}

}
