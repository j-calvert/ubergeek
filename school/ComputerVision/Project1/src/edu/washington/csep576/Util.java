package edu.washington.csep576;

public class Util {
	
	/**
	 * Euclidean distance
	 */
	static double distance(double[] c1, double[] c2) {
		double distance = 0;
		for (int i = 0; i < c1.length; i++) {
			distance += Math.pow(c1[i] - c2[i], 2);
		}
		return Math.sqrt(distance);
	}

	/**
	 * Component-wise average
	 */
	static double[] ave(double[] c1, double[] c2) {
		double[] ave = sum(c1, c2);
		for (int i = 0; i < ave.length; i++) {
			ave[i] = ave[i] / 2;
		}
		return ave;
	}

	/**
	 * Component-wise sum
	 */
	static double[] sum(double[] c1, double[] c2) {
		double[] sum = new double[c1.length];
		for (int i = 0; i < c1.length; i++) {
			sum[i] = c1[i] + c2[i];
		}
		return sum;
	}

	/**
	 * [ Y ]   [ 0.299 0.587 0.114   ] [ R ]
	 * [ I ] = [ 0.596 -0.275 -0.321 ] [ G ] 
	 * [ Q ]   [ 0.212 -0.523 0.311  ] [ B ]
	 */
	static double[] rgb2yiq(double[] rgb) {
		double[] yiq = new double[3];
		yiq[0] = 0.299f * rgb[0] + 0.587f * rgb[1] + 0.114f  * rgb[2];
		yiq[1] = 0.596f * rgb[0] - 0.275f * rgb[1] - 0.321f  * rgb[2];
		yiq[2] = 0.212f * rgb[0] - 0.523f * rgb[1] + 0.311f  * rgb[2];
		return yiq;
	}
	
	/**
	 * [ R ]     [ 1   0.956   0.621 ] [ Y ]
     * [ G ]  =  [ 1  -0.272  -0.647 ] [ I ]
     * [ B ]     [ 1  -1.105   1.702 ] [ Q ]
	 */
	static double[] yiq2rgb(double[] yiq){
		double[] rgb = new double[3];
		rgb[0] = yiq[0] + 0.956f * yiq[1] + 0.621f * yiq[2];
		rgb[1] = yiq[0] - 0.272f * yiq[1] - 0.647f * yiq[2];
		rgb[2] = yiq[0] - 1.105f * yiq[1] + 1.702f * yiq[2];
		return rgb;
	}
	
}
