package edu.washington.csep576;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Homography {
	
	private double[][] matrix = new double[3][3];
	
	public Homography(File file){
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			for(int i = 0; i < 3; i++){
				String parts[] = in.readLine().trim().split("\\s+");
				if(parts.length != 3){
					throw new RuntimeException(file + " has invalid format");
				}
				for(int j = 0; j < parts.length; j++){
					try {
						matrix[i][j] = Double.parseDouble(parts[j]);
					} catch (NumberFormatException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	double[] transform(double x, double y){
		double d = matrix[2][0] * x + matrix[2][1] * y + matrix[2][2];
		double[] ret = new double[2];
		for(int i = 0; i <= 1; i++){
			ret[i] = (matrix[i][0] * x + matrix[i][1] * y + matrix[i][2]) / d;
		}
		return ret;
	}
}
