package edu.washington.csep576.images;

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
	
	private PPM(){}

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
					data[x][y][c] = (1f * in.read() / 255f);
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
	
	public PPM copy() {
		PPM ppm = new PPM();
		ppm.width = this.width;
		ppm.height = this.height;
		ppm.type = this.type;
		ppm.maxval = this.maxval;
		ppm.data = new double[width][height][3];
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				for(int c = 0; c < 3; c++){
					ppm.data[x][y][c] = this.data[x][y][c];
				}
			}
		}
		return ppm;
	}
	
	public PPM copyExpand(int newHeight){
		PPM ppm = new PPM();
		ppm.width = this.width;
		ppm.height = newHeight;
		ppm.type = this.type;
		ppm.maxval = this.maxval;
		ppm.data = new double[ppm.width][ppm.height][3];
		return ppm;
		
	}
}
