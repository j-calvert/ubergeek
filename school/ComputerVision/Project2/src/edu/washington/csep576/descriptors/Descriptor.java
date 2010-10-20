package edu.washington.csep576.descriptors;

import edu.washington.csep576.HarrisDetector;
import edu.washington.csep576.FeatureSet.Feature;

public interface Descriptor {

	Feature describeFeature(HarrisDetector harris, int x, int y);

}
