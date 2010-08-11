package org.kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;

import javax.swing.event.ChangeEvent;

import org.kitebot.Const;
import org.kitebot.gear.GearColor;
import org.kitebot.gear.PlanetaryGear;


public class Kitebot extends Applet {
	private PlanetaryGear planetaryGear;
	private SliderInput windInput;
	private SliderInput genInput;
	private Dial dial;
	
	private Image image;

	private static final double MAX_LINE_FORCE = 200;
	private static final double BRAKE_DRAG = 10;
	private static final double GEN_TORQUE_MAX = 200;
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
		planetaryGear = new PlanetaryGear(200, 200, 40, 20, 20, 60, 8, 4);
		windInput = new SliderInput(450, 0, 30, 300, GearColor.SUN);
		genInput = new SliderInput(500, 0, 30, 300, GearColor.ANNULUS);
		dial = new Dial();
		plots = new ScrollingPlots();
		this.add(planetaryGear);
		this.add(windInput);
		this.add(genInput);
		this.add(dial);
		new Sim().start();
		setSize(600, 600);
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		windInput.paint(gi);
		genInput.paint(gi);
		planetaryGear.paint(gi);
		dial.paint(gi);
		plots.paint(gi);
		g.drawImage(image, 0, 0, null);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void move() {
		long now = System.currentTimeMillis();
		long millisec = now - clock;
		clock = now;
		double sunGearTorque = MAX_LINE_FORCE * windInput.getFraction();
		double delta = millisec * 1d / 1000d;
		double genTorque = planetCarrierSpeed > 0 ? -GEN_TORQUE_MAX : GEN_TORQUE_MAX;
		genTorque = genTorque * genInput.getFraction();
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