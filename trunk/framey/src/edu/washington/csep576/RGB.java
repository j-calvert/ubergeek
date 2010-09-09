package edu.washington.csep576;

import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.util.Random;


public class RGB {
	private static ColorModel model = DirectColorModel.getRGBdefault();

	int r, g, b, a;

	public RGB(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	// Ignore the incoming int's alpha, reuse it or something.
	public RGB(int c, int j) {
		this.r = model.getRed(c);
		this.g = model.getGreen(c);
		this.b = model.getBlue(c);
		this.a = j;
	}

	public void add(RGB that) {
		this.a += that.a;
		this.r += that.r;
		this.g += that.g;
		this.b += that.b;
	}

	public RGB getDivByA() {
		return new RGB(r / a, g / a, b / a, a);
	}
	
	public int getA() {
		return a;
	}


	public float distance(int c) {
		return distance(new RGB(c, 0));
	}
	
	public float distance(RGB that) {
		float distance = 0;
		distance += Math.pow(r - that.r, 2);
		distance += Math.pow(g - that.g, 2);
		distance += Math.pow(b - that.b, 2);
		return (float) Math.sqrt(distance) / 256;	
	}

	public void weightAve(RGB that) {
		int a = this.a + that.a;
		this.r = (this.r * this.a + that.r * that.a)  / a;
		this.g = (this.g * this.a + that.g * that.a)  / a;
		this.b = (this.b * this.a + that.b * that.a)  / a;
		this.a = a;
	}
	
	private static Random rand = new Random();
	public static RGB random() {
		return new RGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 0);
	}
	

}

