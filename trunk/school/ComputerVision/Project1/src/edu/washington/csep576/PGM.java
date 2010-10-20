package edu.washington.csep576;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class PGM extends Pixmap {

	private int[][] data; // x, y

	public PGM(InputStream in) throws IOException {
		super(in);
	}
	
	@Override
	public void read(InputStream in) throws IOException {
		readHeader(in);
		data = new int[width][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				data[x][y] = in.read();
			}
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				out.write(data[x][y]);
			}
		}
	}
}
