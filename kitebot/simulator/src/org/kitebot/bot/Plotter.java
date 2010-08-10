package org.kitebot.bot;

import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;

import org.kitebot.gear.GearColor;

public class Plotter extends Component {
	private static final long serialVersionUID = 1L;

	protected int width, height, posX, posY, lastY;
	private boolean inverted = false;

	private GearColor gColor;

	private Point2D grdA, grdB;
	
	private Queue<PlotPoint> plot = new LinkedList<PlotPoint>();

	public Plotter(int posX, int posY, int width, int height, GearColor gColor) {
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
		this.lastY = height;
		this.gColor = gColor;
		this.grdA = new Point2D.Double(posX, posY);
		this.grdB = new Point2D.Double(posX + width / 2, posY);
		setBounds(new Rectangle(posX, posY, width, height));
	}

	public Plotter(int posX, int posY, int width, int height, GearColor gColor,
			boolean inverted) {
		this(posX, posY, width, height, gColor);
		this.inverted = inverted;
	}

	public void paint(Graphics g) {
		((Graphics2D) g).setPaint(new GradientPaint(grdA,
				(inverted ? gColor.fgClr : gColor.bgClr).darker(), grdB,
				(inverted ? gColor.fgClr : gColor.bgClr).brighter(), true));
		g.fillRect(posX, posY, width, lastY);
		((Graphics2D) g).setPaint(new GradientPaint(grdA,
				(inverted ? gColor.bgClr : gColor.fgClr).darker(), grdB,
				(inverted ? gColor.bgClr : gColor.fgClr).brighter(), true));
		g.fillRect(posX, posY + lastY, width, height - lastY);
		g.setColor(GearColor.INTERIOR.fgClr);
		g.drawRect(posX, posY, width, height);
	}

	public double getFraction() {
		return 1d * (inverted ? lastY : height - lastY) / height;
	}

	public void setFraction(double f) {
		lastY = (int) (inverted ? f * height : (1 - f) * height);
	}

	protected void boundY() {
		lastY = lastY < 0 ? 0 : lastY;
		lastY = lastY > height ? height : lastY;
	}
	
	private static class PlotPoint implements Comparable<PlotPoint> {
		long ts, val;
	
		PlotPoint(long ts, long val) {
			this.ts = ts;
			this.val = val;
		}

		@Override
		public int compareTo(PlotPoint o) {
			return (int) (this.ts - o.ts);
		}		
	}

}
