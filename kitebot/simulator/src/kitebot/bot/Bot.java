package kitebot.bot;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;

import kitebot.gear.PlanetaryGear;

public class Bot extends Applet {
	private PlanetaryGear planetaryGear;
	private Dashboard dashboard;

	private Image image;

	private double genTorque = -5;
	private double planetCarrierSpeed = 1, ringGearSpeed = 2;
	private static final long delta = 10;

	public Bot() {
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
		double sunGearTorque = dashboard.getLineForce();
		double delta = millisec * 1d / 1000d;
		double[] accelerate = Jourdain.accelerate(delta, planetCarrierSpeed, ringGearSpeed,
				genTorque * ringGearSpeed, sunGearTorque);
		planetCarrierSpeed = accelerate[0];
		ringGearSpeed = accelerate[1];
		planetaryGear.move(millisec, planetCarrierSpeed, ringGearSpeed);
		System.out.println(planetCarrierSpeed + " " + ringGearSpeed);
	}

	private class Sim extends Thread {

		@Override
		public void run() {
			long t1, t0 = System.currentTimeMillis();
			while (true) {
				repaint();
				move(delta);
				t1 = System.currentTimeMillis();
				try {
					if (t1 - t0 < delta) {
						Thread.sleep(delta - (t1 - t0));
					}
				} catch (InterruptedException e) {
					return;
				}
				t0 = t1;
			}
		}
	}
}