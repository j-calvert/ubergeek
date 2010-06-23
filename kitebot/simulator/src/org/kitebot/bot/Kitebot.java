package org.kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;

import org.kitebot.gear.PlanetaryGear;


public class Kitebot extends Applet {
	private PlanetaryGear planetaryGear;
	private Dashboard dashboard;

	private Image image;

	private static final double MAX_LINE_FORCE = 50;
	private static final double GEN_TORQUE_BASE = 20;
	private static final int GEN_TORQUE_INCREASE_FACTOR = 10;
	private static final long DELTA = 10;
	private double planetCarrierSpeed = 30, ringGearSpeed = 40;

	public Kitebot() {
		super();
		enableEvents(MouseEvent.MOUSE_EVENT_MASK);
		enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		setEnabled(true);
	}

	public void init() {
		setLayout(null);
		setBackground(Color.WHITE);
		setSize(600, 600);
		image = createImage(600, 520);
		planetaryGear = new PlanetaryGear(200, 200, 40, 20, 20, 60, 8, 4);
		dashboard = new Dashboard(450, 20, 50, 400);
		this.add(planetaryGear);
		this.add(dashboard);
		new Sim().start();
	}

	public void paint(Graphics g) {
		Graphics gi = image.getGraphics();
		dashboard.paint(gi);
		planetaryGear.paint(gi);
		g.drawImage(image, 0, 0, null);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void move(long millisec) {
		double sunGearTorque = MAX_LINE_FORCE * dashboard.getLineForceFraction();
		double delta = millisec * 1d / 1000d;
		double genTorque = planetCarrierSpeed > 0 ? GEN_TORQUE_BASE : GEN_TORQUE_BASE;
		genTorque = genTorque * (1 + planetCarrierSpeed / GEN_TORQUE_INCREASE_FACTOR);
		double[] accelerate = Jourdain.accelerate(delta, planetCarrierSpeed, ringGearSpeed,
				genTorque, sunGearTorque);
		planetCarrierSpeed = accelerate[0];
		ringGearSpeed = accelerate[1];
		planetaryGear.move(millisec, planetCarrierSpeed, ringGearSpeed);
//		System.out.println("spd: " + PlanetaryGear.computeSunGearSpeed(planetCarrierSpeed,
//				ringGearSpeed) + " " + planetCarrierSpeed + " " + ringGearSpeed + " trq: " + sunGearTorque + " " + genTorque);
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