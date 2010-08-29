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
		int thisX = (int) (lastF[0] * width);
		
		Graphics2D g2 = (Graphics2D) g;

		if(this instanceof SliderInput) {
			g2.setColor(Color.BLACK);
		} else {
			g2.setColor(Color.GRAY);			
		}
		g2.drawRect(posX - 1, posY -  1, width + 1, height + 1);
		
		g2.setPaint(Color.white);
		g2.fillRect(0, posY - 1, posX - 1, height + 1);
		g2.fillRect(posX + width + 1, posY - 1, 100, height + 1);

		g2.setPaint(new GradientPaint(grdA, bgClr.darker(), grdB, bgClr.brighter(), true));
		g2.fillRect(posX + thisX, posY, width - thisX, height);

		int prevX = posX;
		for(int i = 0; i < lastF.length; i++) {
			thisX = (int) (lastF[i] * width);
			g2.setPaint(new GradientPaint(grdA, gColor[i].fgClr.darker(), grdB, gColor[i].fgClr.brighter(), true));
			if(thisX > 0) {
				g2.fillRect(prevX, posY + 2 * i, thisX, height - 4 * i);
			} else {
				g2.fillRect(prevX + thisX, posY + 2 * i, -thisX, height - 4 * i);				
			}
			prevX = thisX + prevX;
		}
	}
	
	public void setFraction(int i, double f) {
		lastF[i] = f;
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
