package edu.washington.csep576;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sleepycat.persist.model.Persistent;

/**
 * A base class for Portable Pixel Maps.
 */
@Persistent
public abstract class Pixmap {

	/*
	 * Properties common to all PPMs.
	 */
	protected int type = 6, width, height, maxval = 255;
	
	protected Pixmap(){}
	
	/**
	 * Construct a new Pixmap. Image data supplied by the provided InputStream
	 * (e.g. a FileInputStream).
	 */

	public Pixmap(InputStream in) throws IOException {
		read(in);
	}

	/**
	 * To be implemented by subclass, but each implementation should make use of readHeader.
	 */
	public abstract void read(InputStream in) throws IOException;

	/**
	 * To be implemented by subclass, but each implementation should make use of writeHeader.
	 */
	public abstract void write(OutputStream out) throws IOException;
	
	/**
	 * Read the image metadata.
	 */
	protected void readHeader(InputStream in) throws IOException {
		String line = readLine(in);
		String[] parts = line.split("\\s+");
		if (!parts[0].startsWith("P")) {
			throw new IOException("Invalid magic format string " + parts[0]);
		}
		try {
			type = Integer.parseInt(parts[0].substring(1));
			width = Integer.parseInt(parts[1]);
			height = Integer.parseInt(parts[2]);
			maxval = Integer.parseInt(parts[3]);
		} catch (NumberFormatException e) {
			throw new IOException("Invalid header integer value " + e.getMessage());
		}
		
	}
	
	private static String readLine(InputStream in) throws IOException {
		char c;
		String line = "";
		do {
			c = (char) in.read();
			line += c;
		} while (c != '\n');
		return line;
	}

	/**
	 * Write the image metadata.
	 */
	protected void writeHeader(OutputStream out) throws IOException {
		out
		.write(("P" + type + " " + width + " " + height + " " + maxval + "\n")
				.getBytes());

	}

	/**
	 * Get the image's width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the image's height.
	 */
	public int getHeight() {
		return height;
	}
	
	

}
