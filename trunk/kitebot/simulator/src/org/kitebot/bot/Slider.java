package org.kitebot.bot;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.kitebot.gear.GearColor;

public class Slider extends Component {
	private static final long serialVersionUID = 1L;

	protected int width, height, posX, posY;
	protected double[] lastF;

	private GearColor[] gColor;
	private Color bgClr = Color.lightGray;

	private Point2D grdA, grdB;

	public Slider(int posX, int posY, int width, int height, GearColor[] gColor) {
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
		this.lastF = new double[gColor.length];
		this.lastF[0] = 1;
		this.gColor = gColor;
		this.grdA = new Point2D.Double(posX, posY);
		this.grdB = new Point2D.Double(posX, posY + height / 2);
		setBounds(new Rectangle(posX, posY, width, height));
	}

	public void paint(Graphics g) {
		int lastX = (int) (lastF[0] * width);
		lastX = boundX(lastX);

		if(this instanceof SliderInput) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.GRAY);			
		}
		g.drawRect(posX - 1, posY -  1, width + 1, height + 1);

		((Graphics2D) g).setPaint(new GradientPaint(grdA,
				gColor[0].fgClr.darker(), grdB, gColor[0].fgClr.brighter(), true));
		g.fillRect(posX, posY, lastX, height);

		((Graphics2D) g).setPaint(new GradientPaint(grdA,
				bgClr.darker(), grdB, bgClr.brighter(), true));
		g.fillRect(posX + lastX, posY, width - lastX, height);
	}
	
	public void setFraction(double f) {
		lastF[0] = f;
	}
	
	public double[] getLastF() {
		return lastF;
	}

	protected int boundX(int x) {
		if(x < 0) {
			return 0;
		} else if (x > width) {
			return width;
		} else {
			return x;
		}
	}

}
