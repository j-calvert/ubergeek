package org.kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
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
		setEnabled(true);
	}

	public void init() {
		super.init();
		init(120);
	}

	private void init(int expnsn) {
		setLayout(null);
		setBackground(Color.WHITE);
		image = createImage(600, 600);
		planetaryGear = new PlanetaryGear(245 * expnsn / 100,
				175 * expnsn / 100, 17 * expnsn / 100, 20, 20, 60, 4);
		windInput = new SliderInput(50 * expnsn / 100, 10 * expnsn / 100,
				500 * expnsn / 100, 30 * expnsn / 100, new GearColor[] {
						GearColor.SUN, GearColor.BRAKE, GearColor.LINE });
		gennie = new SliderInput(50 * expnsn / 100, 50 * expnsn / 100,
				500 * expnsn / 100, 20, new GearColor[] { GearColor.ANNULUS,
						GearColor.BRAKE });
		accelerationDial = new AccelerationDial(new Rectangle2D.Float(
				10 * expnsn / 100, 100 * expnsn / 100, 150 * expnsn / 100,
				150 * expnsn / 100));
		velocityDial = new VelocityDial(new Rectangle2D.Float(
				10 * expnsn / 100, 260 * expnsn / 100, 150 * expnsn / 100,
				150 * expnsn / 100));
		positionDial = new PositionDial(new Rectangle2D.Float(
				170 * expnsn / 100, 260 * expnsn / 100, 150 * expnsn / 100,
				150 * expnsn / 100));
		plots = new ScrollingPlots(new Rectangle2D.Float(50 * expnsn / 100,
				400 * expnsn / 100, 500 * expnsn / 100, 200 * expnsn / 100));
		this.add(planetaryGear);
		this.add(windInput);
		this.add(gennie);
		this.add(velocityDial);
		this.add(positionDial);
		new Sim().start();
		setSize(600, 600);
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		windInput.paint(gi);
		gennie.paint(gi);
		accelerationDial.paint(gi);
		velocityDial.paint(gi);
		positionDial.paint(gi);
		plots.paint(gi);
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