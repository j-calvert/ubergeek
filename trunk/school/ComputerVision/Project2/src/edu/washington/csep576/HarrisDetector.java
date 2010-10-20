package edu.washington.csep576;

import java.util.ArrayList;
import java.util.List;

import edu.washington.csep576.images.PPM;

public class HarrisDetector {

	double k = 0.051;
	double R = 0.001;

	int minCandidates = 100;

	int width, height;

	// The raw image data, for writing back out after annotating
	PPM ppm;
	// The intensity at point x, y. Constructed in readGreytones from a PPM.
	// Normalized to be in range [0,1]
	double[][] greytones;
	// I_x and I_y for each point x, y (that's at least distance)
	// 1 from the border.
	double[][][] gradients;
	// The corner response as defined in the assignment description
	// (Dependent on the choice of k).
	double[][] cornerResponse;

	void loadPPM(PPM ppm) {
		this.ppm = ppm;
		width = ppm.getWidth();
		height = ppm.getHeight();
		greytones = new double[width][height];
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double intensity = length(ppm.getColor(x, y));
				this.greytones[x][y] = intensity;
				if (intensity > max) {
					max = intensity;
				}
				if (intensity < min) {
					min = intensity;
				}
			}
		}
		// Now normalize. Pretty inconsequential, but why not.
		if (max <= min) {
			throw new RuntimeException("Image intensity max <= min");
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				greytones[x][y] = (greytones[x][y] - min) / (max - min);
			}
		}
	}

	void computeGradients() {
		gradients = new double[width][height][2];

		int[][] sobel = new int[3][3];
		sobel[0][0] = 1;
		sobel[0][1] = 0;
		sobel[0][2] = -1;
		sobel[1][0] = 2;
		sobel[1][1] = 0;
		sobel[1][2] = -2;
		sobel[2][0] = 1;
		sobel[2][1] = 0;
		sobel[2][2] = -1;
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				gradients[x][y][0] = gradients[x][y][1] = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						gradients[x][y][0] += greytones[x + i - 1][y + j - 1]
								* sobel[i][j];
						// Sobel matrix for I_y is transpose of matrix for I_x.
						gradients[x][y][1] += greytones[x + i - 1][y + j - 1]
								* sobel[j][i];
					}
				}
			}
		}
	}

	static double length(double[] c){
		double distance = 0;
		for (int i = 0; i < c.length; i++) {
			distance += Math.pow(c[i], 2);
		}
		return Math.sqrt(distance);		
	}

	
	void computeCornerResponse() {
		cornerResponse = new double[width][height];
		
		int numCandidates = 0;
		// We'll keep trying, decreasing k by 0.001 until we get at least
		// minCandidates
		// or k "bottoms out" at 0.01.
		while (numCandidates < minCandidates && k > 0.01) {
			k += -.001;
			numCandidates = 0;
			for (int x = 3; x < width - 3; x++) {
				for (int y = 3; y < height - 3; y++) {
					// [ a b ] <-- representation of 2x2 Harris matrix.
					// [ c d ] For us b == c.
					double a = 0, b = 0, d = 0;
					for (int i = -2; i <= 2; i++) {
						for (int j = -2; j <= 2; j++) {
							double weight = getWeight(i, j);
							a = a + weight * gradients[x + i][y + j][0]
									* gradients[x + i][y + j][0];
							b = b + weight * gradients[x + i][y + j][0]
									* gradients[x + i][y + j][1];
							d = d + weight * gradients[x + i][y + j][1]
									* gradients[x + i][y + j][1];
						}
					}
					cornerResponse[x][y] = (a * d - b * b) - k * (a + d)
							* (a + d);
					if (cornerResponse[x][y] > R) {
						numCandidates++;
					}
				}
			}
		}
	}

	/**
	 * Returns points that have a corner response that is a local maximum in a
	 * square of side length 2 * rad + 1.
	 * 
	 * @param rad
	 * @return
	 */
	int[][] chooseFeatureLocations(int rad) {
		List<int[]> intList = new ArrayList<int[]>();
		for(int x = 10; x < width - 10; x++){
			for(int y = 10; y < height - 10; y++){
				if(cornerResponse[x][y] > R){
					boolean isMax = true;
					for(int i = -rad; i <= rad; i++){
						for(int j = -rad; j <= rad; j++){
							if(cornerResponse[x + i][y + j] > cornerResponse[x][y]){
								isMax = false;
								break;
							}
						}
						if(!isMax){
							break;
						}
					}
					if(isMax){
						intList.add(new int[]{x, y});
					}
				}
			}
		}
		int[][] ret = new int[intList.size()][];
		for(int i = 0; i < intList.size(); i++){
			ret[i] = intList.get(i);
		}
		return ret;
	}

	// Returns the value of the 5x5 gaussian mask provided in the assigment
	// description.
	private double getWeight(int rel_x, int rel_y) {
		double scale = 159;
		int x = rel_x + 2;
		int y = rel_y + 2;
		if (x > 2) {
			x = 4 - x;
		}
		if (y > 2) {
			y = 4 - y;
		}
		if (x > y) {
			int t = x;
			x = y;
			y = t;
		}
		if (x == 0) {
			if (y == 0) {
				return 2 / scale;
			} else if (y == 1) {
				return 4 / scale;
			} else {
				return 5 / scale;
			}
		} else if (x == 1) {
			if (y == 1) {
				return 9 / scale;
			} else {
				return 12 / scale;
			}
		} else {
			return 15 / scale;
		}
	}
	
	public double getPixel(int x, int y){
		return greytones[x][y];
	}
	
	public double getGradient(int x, int y, int xOrY){
		if(xOrY != 0 && xOrY != 1){
			throw new RuntimeException("Trying to get gradient in dimension != 2");
		}
		return gradients[x][y][xOrY];
	}
}
