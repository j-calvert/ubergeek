package edu.washington.csep576;

import java.io.IOException;
import java.io.InputStream;

public class SpectrumPlotter extends PPM {	
	
	SpectrumPlotter(double[][] spectrum) throws IOException{
		super(null);
		this.width = 50;
		this.height = 16 * spectrum.length;
		this.data = new double[width][height][3];
		for(int i = 0; i < spectrum.length ; i++){
			for(int y = 16 * i; y < 16 * (i + 1); y ++){
				for(int x = 0; x < 50; x++){
					data[x][y] = spectrum[i];
				}
			}
		}
	}
	
	public void read(InputStream in) throws IOException{}
}
