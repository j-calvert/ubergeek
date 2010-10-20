package edu.washington.csep576.descriptors;

import edu.washington.csep576.HarrisDetector;
import edu.washington.csep576.FeatureSet.Feature;

public class SimpleDescriptor implements Descriptor {

	@Override
	public Feature describeFeature(HarrisDetector harris, int x, int y){
		double[] chars = new double[25];
		double total = 0;
		int n = 0;
		for(int i = x - 2; i <= x + 2; i++){
			for(int j = y - 2; j <= y + 2; j++){
				chars[n] = harris.getPixel(i, j);
				total += Math.pow(chars[n], 2);
				n++;
			}			
		}
		total = Math.sqrt(total);
		for(int i = 0; i < chars.length; i++){
			chars[i] /= total;
		}
		return new Feature(x, y, chars);
	}

}
