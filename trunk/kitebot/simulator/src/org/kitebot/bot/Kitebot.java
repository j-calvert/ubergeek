package org.kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.event.ChangeEvent;

import org.kitebot.Const;
import org.kitebot.bot.unused.ScrollingPlots;
import org.kitebot.gear.GearColor;
import org.kitebot.gear.PlanetaryGear;

public class Kitebot extends Applet {
	private PlanetaryGear planetaryGear;
	private SliderInput windInput;
	private Slider gennie;
	private AccelerationDial accelerationDial;
	private VelocityDial velocityDial;
	private PositionDial positionDial;

	private Image image;

	private static final double MAX_LINE_FORCE = 100;
	private static final double BRAKE_DRAG = 50;
	private static final double WIND_DRAG = .2;
	private static final double GEN_TORQUE_MAX = 100;
	private double planetCarrierSpeed = 45, ringGearSpeed = 80;
	private ScrollingPlots plots;

	private long clock = System.currentTimeMillis();

	public Kitebot() {
		super();
		enableEvents(MouseEvent.MOUSE_EVENT_MASK);
		enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		enableEvents(ComponentEvent.COMPONENT_RESIZED);
		setEnabled(true);
	}

	@Override
//	protected void processComponentEvent(ComponentEvent e) {
//		System.err.println("ID: " + e.getID());
//		System.err.println("RESIZE");
//		if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
//			Rectangle b = (e.getSource() != null ? ((Component) e.getSource())
//					.getBounds() : null);
//			if (b != null) {
//				init(b.width / scale, b.height / scale);
//			}
//		}
//	}

	public void init() {
		super.init();
		init(120, 100);
		setSize(scale(120), scale(100));
		new Sim().start();
	}

	private int scale = 6;

	int scale(int i) {
		return scale * i;
	}

	private void init(int fctx, int fcty) {
		int fct = Math.min(fctx, fcty);
		setLayout(null);
		setBackground(Color.WHITE);
		System.out.println("Resizing with fct: " + fct);
		image = createImage(scale(fctx), scale(fcty));

		int border = scale(fct / 20);
		int sliderWidth = scale(fctx) - 2 * border;
		int sliderHeight = scale(fcty * 1 / 15);
		int plotterHeight = scale(fcty * 3 / 10);
		
		int currentTop = border;
		windInput = new SliderInput(border, currentTop, sliderWidth, sliderHeight,
				new GearColor[] { GearColor.SUN, GearColor.BRAKE,
						GearColor.LINE });

		currentTop += border + sliderHeight;
		gennie = new SliderInput(border, currentTop, sliderWidth,
				sliderHeight, new GearColor[] { GearColor.ANNULUS,
						GearColor.BRAKE });

		currentTop += border + sliderHeight;
		int I = 2, J = 2;
		int cellSize = Math.min((scale(fctx) - border) / I, (scale(fcty) - (2 * (sliderHeight + border) + plotterHeight + border)) / J);
		int left = Math.max(border, scale(fctx) / 2 - ((cellSize + border) * I / 2 + border));
		System.out.println("cellSize: " + cellSize);
		Rectangle2D.Float[][] rects = new Rectangle2D.Float[I][J];
		for (int i = 0; i < I; i++) {
			for (int j = 0; j < J; j++) {
				rects[i][j] = new Rectangle2D.Float((border + cellSize) * i
						+ left, currentTop + (border + cellSize) * j, cellSize, cellSize);
			}
		}

		planetaryGear = new PlanetaryGear((int) (rects[0][1].x + cellSize / 2),
				(int) (rects[0][1].y + cellSize / 2), cellSize / 10, 20, 20,
				60, 4);
		accelerationDial = new AccelerationDial(rects[0][0]);
		velocityDial = new VelocityDial(rects[1][0]);
		positionDial = new PositionDial(rects[1][1]);
		plots = new ScrollingPlots(new Rectangle2D.Float(border, scale(fcty) - plotterHeight - border, sliderWidth, plotterHeight));
		this.add(planetaryGear);
		this.add(windInput);
		this.add(gennie);
		this.add(velocityDial);
		this.add(positionDial);
		this.add(plots);
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		windInput.paint(gi);
		gennie.paint(gi);
		plots.paint(gi);
		accelerationDial.paint(gi);
		velocityDial.paint(gi);
		positionDial.paint(gi);
		planetaryGear.paint(gi);
		g.drawImage(image, 0, 0, null);
	}

	public void update(Graphics g) {
		paint(g);
	}

	// The big do-it method
	public long move() {
		long now = System.currentTimeMillis();
		long millisec = now - clock;
		clock = now;
		double sunGearTorque = MAX_LINE_FORCE * windInput.getLastF()[0];
		double delta = millisec * 1d / 1000d;
		double genTorque = planetCarrierSpeed > 0 ? -GEN_TORQUE_MAX
				: GEN_TORQUE_MAX;
		genTorque = genTorque * gennie.getLastF()[0];
		double sunGearSpeed = Const.planetaryRelation(planetCarrierSpeed,
				ringGearSpeed);
		double brakeTorque = 0;
		double dragTorque = 0;
		double genBrakeTorque = 0;

		if (sunGearTorque < -genTorque) {
			if (sunGearSpeed > .01) {
				brakeTorque = -BRAKE_DRAG * (sunGearSpeed + .1);
			} else if (sunGearSpeed > 0) {
				brakeTorque = -sunGearTorque;
			}
		}

		if (sunGearSpeed < -.0001) {
			dragTorque = -WIND_DRAG * sunGearSpeed;
		}

		// TODO Start here...get this right.
		// if(2 * sunGearTorque < -genTorque && sunGearSpeed > 0) {
		// if(planetCarrierSpeed > .01) {
		// genBrakeTorque = -BRAKE_DRAG * (planetCarrierSpeed + .1);
		// }
		// }
		double[] accelerate = Jourdain.accelerate(delta, planetCarrierSpeed,
				ringGearSpeed, genTorque + genBrakeTorque, sunGearTorque
						+ brakeTorque + dragTorque);
		planetCarrierSpeed = accelerate[0];
		ringGearSpeed = accelerate[1];
		planetaryGear.move(millisec, planetCarrierSpeed, ringGearSpeed);
		windInput.setFraction(1, brakeTorque / MAX_LINE_FORCE);
		windInput.setFraction(2, dragTorque / MAX_LINE_FORCE);
		gennie.setFraction(1, genBrakeTorque / GEN_TORQUE_MAX);
		ChangeEvent changeevent = new ChangeEvent(new double[] { ringGearSpeed,
				planetCarrierSpeed, sunGearSpeed });
		accelerationDial.stateChanged(changeevent);
		velocityDial.stateChanged(changeevent);
		plots.stateChanged(changeevent);
		positionDial.stateChanged(new ChangeEvent(new double[] {
				planetaryGear.getSunAngle(), planetaryGear.getSunAngle() }));

		// System.out.println("spd: " +
		// Const.planetaryRelation(planetCarrierSpeed,
		// ringGearSpeed) + " " + planetCarrierSpeed + " " + ringGearSpeed +
		// " trq: " + sunGearTorque + " " + genTorque);
		return System.currentTimeMillis() - now;
	}

	private class Sim extends Thread {
		@Override
		public void run() {
			while (true) {
				repaint();
				try {
					Thread.sleep(Math.max(0, 50 - move()));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}