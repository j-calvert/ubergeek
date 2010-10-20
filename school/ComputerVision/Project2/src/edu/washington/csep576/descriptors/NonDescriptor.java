package edu.washington.csep576.descriptors;

import edu.washington.csep576.HarrisDetector;
import edu.washington.csep576.FeatureSet.Feature;

public class NonDescriptor implements Descriptor {

	@Override
	public Feature describeFeature(HarrisDetector harris, int x, int y) {
		return new Feature(x, y);
	}
}
