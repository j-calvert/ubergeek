package org.kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.event.ChangeEvent;

import org.kitebot.Const;
import org.kitebot.gear.GearColor;
import org.kitebot.gear.PlanetaryGear;


public class Kitebot extends Applet {
	private PlanetaryGear planetaryGear;
	private SliderInput windInput;
	private Slider gennie;
	private Dial dial;
	private AltitudeDial altitudeDial;
	
	private Image image;

	private static final double MAX_LINE_FORCE = 200;
	private static final double BRAKE_DRAG = 10;
	private static final double GEN_TORQUE_MAX = 200;
	private static final int LINE_RATIO = 1;
	private double planetCarrierSpeed = 45, ringGearSpeed = 60;
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
		setLayout(null);
		setBackground(Color.WHITE);
		image = createImage(600, 600);
		planetaryGear = new PlanetaryGear(300, 270, 30, 20, 20, 60, 4);
		windInput = new SliderInput(50, 10, 500, 30, new GearColor[] {GearColor.SUN});
		gennie = new SliderInput(50, 50, 500, 30, new GearColor[] {GearColor.ANNULUS});
		dial = new Dial(new Rectangle2D.Float(50, 100, 150, 150));
		altitudeDial = new AltitudeDial(new Rectangle2D.Float(400, 100, 150, 150));
		plots = new ScrollingPlots(new Rectangle2D.Float(50, 400, 500, 200));
		this.add(planetaryGear);
		this.add(windInput);
		this.add(gennie);
		this.add(dial);
		this.add(altitudeDial);
		new Sim().start();
		setSize(600, 600);
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		windInput.paint(gi);
		gennie.paint(gi);
		dial.paint(gi);
		altitudeDial.paint(gi);
		plots.paint(gi);
		planetaryGear.paint(gi);
		g.drawImage(image, 0, 0, null);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void move() {
		long now = System.currentTimeMillis();
		long millisec = now - clock;
		clock = now;
		double sunGearTorque = MAX_LINE_FORCE * windInput.getLastF()[0];
		double delta = millisec * 1d / 1000d;
		double genTorque = planetCarrierSpeed > 0 ? -GEN_TORQUE_MAX : GEN_TORQUE_MAX;
		genTorque = genTorque * gennie.getLastF()[0];
		double sunGearSpeed = Const.planetaryRelation(planetCarrierSpeed, ringGearSpeed);
		double brakeTorque = 0;
		boolean brake = false;
		if(sunGearTorque < -genTorque && sunGearSpeed > 0) {
			brakeTorque = -BRAKE_DRAG * sunGearSpeed;
			brake = true;
		}
		double[] accelerate = Jourdain.accelerate(delta, planetCarrierSpeed, ringGearSpeed,
				genTorque, sunGearTorque + brakeTorque, brake);
		planetCarrierSpeed = accelerate[0];
		ringGearSpeed = accelerate[1];
		planetaryGear.move(millisec, planetCarrierSpeed, ringGearSpeed);
		ChangeEvent changeEvent = new ChangeEvent(new Datapoint(clock, ringGearSpeed, planetCarrierSpeed, sunGearSpeed));
		dial.stateChanged(changeEvent);
		plots.stateChanged(changeEvent);
		ChangeEvent changeEvent2 = new ChangeEvent(new Double(planetaryGear.getSunAngle() + 100));
		altitudeDial.stateChanged(changeEvent2);
		
//		System.out.println("spd: " + Const.planetaryRelation(planetCarrierSpeed,
//				ringGearSpeed) + " " + planetCarrierSpeed + " " + ringGearSpeed + " trq: " + sunGearTorque + " " + genTorque);
	}

	private class Sim extends Thread {
		@Override
		public void run() {
			while (true) {
				repaint();
				move();
			}
		}
	}
}