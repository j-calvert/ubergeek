package org.kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;

import org.kitebot.Const;
import org.kitebot.gear.PlanetaryGear;


public class Kitebot extends Applet {
	private PlanetaryGear planetaryGear;
	private SliderInput windInput;
	private SliderInput genInput;

	private Image image;

	private static final double MAX_LINE_FORCE = 50;
	private static final double GEN_TORQUE_BASE = 20;
	private static final int GEN_TORQUE_INCREASE_FACTOR = 1;
	private static final long DELTA = 10;
	private double planetCarrierSpeed = 90, ringGearSpeed = 120;

	public Kitebot() {
		super();
		enableEvents(MouseEvent.MOUSE_EVENT_MASK);
		enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		setEnabled(true);
	}

	public void init() {
		setSize(600, 600);
		setLayout(null);
		setBackground(Color.WHITE);
		image = createImage(600, 600);
		planetaryGear = new PlanetaryGear(200, 200, 40, 20, 20, 60, 8, 4);
		windInput = new SliderInput(450, 20, 30, 400);
		genInput = new SliderInput(500, 20, 30, 400);
		genInput.setFgColor(Color.orange);
		this.add(planetaryGear);
		this.add(windInput);
		this.add(genInput);
		new Sim().start();
		setSize(600, 600);
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		windInput.paint(gi);
		genInput.paint(gi);
		planetaryGear.paint(gi);
		g.drawImage(image, 0, 0, null);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void move(long millisec) {
		double sunGearTorque = MAX_LINE_FORCE * windInput.getForceFraction();
		double delta = millisec * 1d / 1000d;
		double genTorque = planetCarrierSpeed > 0 ? -GEN_TORQUE_BASE : GEN_TORQUE_BASE;
		genTorque = genTorque * (1 + GEN_TORQUE_INCREASE_FACTOR * genInput.getForceFraction());
		double sunGearSpeed = Const.planetaryRelation(planetCarrierSpeed, ringGearSpeed);
		if(sunGearTorque < -genTorque && sunGearSpeed > 0) {
			sunGearTorque = genTorque;
		}
		double[] accelerate = Jourdain.accelerate(delta, planetCarrierSpeed, ringGearSpeed,
				genTorque, sunGearTorque);
		planetCarrierSpeed = accelerate[0];
		ringGearSpeed = accelerate[1];
		planetaryGear.move(millisec, planetCarrierSpeed, ringGearSpeed);
		System.out.println("spd: " + Const.planetaryRelation(planetCarrierSpeed,
				ringGearSpeed) + " " + planetCarrierSpeed + " " + ringGearSpeed + " trq: " + sunGearTorque + " " + genTorque);
	}

	private class Sim extends Thread {

		@Override
		public void run() {
			long t1, t0 = System.currentTimeMillis();
			while (true) {
				repaint();
				move(DELTA);
				t1 = System.currentTimeMillis();
				try {
					if (t1 - t0 < DELTA) {
						Thread.sleep(DELTA - (t1 - t0));
					}
				} catch (InterruptedException e) {
					return;
				}
				t0 = t1;
			}
		}
	}
}