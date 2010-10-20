package edu.washington.csep576.descriptors;

import edu.washington.csep576.HarrisDetector;
import edu.washington.csep576.FeatureSet.Feature;

public class AdvancedDescriptor implements Descriptor {

	@Override
	public Feature describeFeature(HarrisDetector harris, int x, int y) {
		double[] chars = new double[81];
		double theta = findDominantAngle(harris, x, y);
		int k = 0;
		double total = 0;
		for(int i = -4; i <= 4; i++){
			for(int j = -4; j <= 4; j++){
				double delta[] = rotate(i, j, theta);
				double xd = x + delta[0];
				double yd = y + delta[1];
				double val = weightedAverage(harris, xd, yd);
				chars[k] = val;
				total += Math.pow(val, 2);
			}
		}
		// And normalize by the SSD as we do with the SimpleDescriptor.
		total = Math.sqrt(total);
		for(int i = 0; i < chars.length; i++){
			chars[i] /= total;
		}
		return new Feature(x, y, chars);
	}
	
	private double findDominantAngle(HarrisDetector harris, int x, int y){
		double[] bins = new double[36];
		for(int i = x - 2; i <= x + 2; i++){
			for(int j = y - 2; j <= y + 2; j++){
				double i_x = harris.getGradient(i, j, 0);
				double i_y = harris.getGradient(i, j, 1);
				double length = Math.sqrt(Math.pow(i_x, 2) + Math.pow(i_y, 2));
				if(length == 0){ continue; }
				double radians = 0;
				if(i_x == 0){
					if(i_y < 0){
						radians = -Math.PI;
					} else {
						radians = Math.PI;
					}	
				} else {
					radians = Math.atan(i_y/i_x);
				}
				int k = (int) Math.floor((radians / Math.PI * 180 + 180) / 10);
				if(k == 36){ k = 0; }
				bins[k] += length;
			}
		}
		double max = Double.MIN_VALUE;
		double maxIdx= 0;
		for(int k = 0; k < bins.length; k++){
			if(bins[k] > max){
				max = bins[k];
				maxIdx = k;
			}
		}
		return (maxIdx - 180.0d) * Math.PI / 180.0d;
	}
	
	double[] rotate(int i, int j, double theta){
		double[] delta = new double[2];
		delta[0] = i * Math.cos(theta) - j * Math.sin(theta);
		delta[1] = i * Math.sin(theta) + j * Math.cos(theta);
		return delta;
	}
	
	double weightedAverage(HarrisDetector harris, double x, double y){
		double x0 = Math.floor(x);
		double x1 = Math.ceil(x);
		double y0 = Math.floor(y);
		double y1 = Math.ceil(y);
		if(x0 < x1 && y0 < y1){
			double 
			ret  = harris.getPixel((int) x0, (int) y0)*(x1 - x)*(y1 - y);
			ret += harris.getPixel((int) x1, (int) y0)*(x - x0)*(y1 - y);
			ret += harris.getPixel((int) x0, (int) y1)*(x1 - x)*(y - y0);
			ret += harris.getPixel((int) x1, (int) y1)*(x - x0)*(y - y0);
			// ret = ret / ((x1 - x0) * (y1 - y0)); Not necessary, 1 by definition
			return ret;
		} else if(x0 < x1){
			double 
			ret  = harris.getPixel((int) x0, (int) y0)*(x1 - x);
			ret += harris.getPixel((int) x1, (int) y0)*(x - x0);
			return ret;
		} else if(y0 < y1){
			double
			ret  = harris.getPixel((int) x0, (int) y0) * (y1 - y);
			ret += harris.getPixel((int) x0, (int) y1) * (y - y0);
			return ret;
		} else {
			return harris.getPixel((int) x, (int) y);
		}
	}
	
}
