package edu.washington.csep576;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class PPM extends Pixmap {

	/*
	 * The underlying image data.  Color coordinate agnostic.
	 */
	protected double[][][] data;

	/**
	 * Construct a new PPM. Image data supplied by the provided InputStream
	 * (e.g. a FileInputStream).
	 */
	public PPM(InputStream in) throws IOException {
		super(in);
	}

	/**
	 * Read the image (here we assume that the color coordinates are RGB).
	 */
	@Override
	public void read(InputStream in) throws IOException {
		readHeader(in);
		data = new double[width][height][3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int c = 0; c < 3; c++) {
					data[x][y][c] = (1f * in.read() / 255);
				}
			}
		}
	}

	/**
	 * Write the image (here we assume that the color coordinates are RGB).
	 */
	@Override
	public void write(OutputStream out) throws IOException {
		writeHeader(out);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int c = 0; c < 3; c++) {
					double cl = data[x][y][c];
					cl = Math.max(cl, 0);
					cl = Math.min(cl, 1);
					out.write((int) Math.round(cl * 255));
				}
			}
		}
	}

	/**
	 * Get color at location x, y (coordinate system-agnostic).
	 */
	public double[] getColor(int x, int y) {
		return data[x][y];
	}

	/**
	 * Set color at location x, y to c (coordinate system-agnostic).
	 */
	public void setColor(int x, int y, double[] c) {
		data[x][y] = c;
	}
	
	public void rgb2yiq(){
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				data[x][y] = Util.rgb2yiq(data[x][y]);
			}
		}
	}

	public void yiq2rgb(){
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				data[x][y] = Util.yiq2rgb(data[x][y]);
			}
		}
	}
}
