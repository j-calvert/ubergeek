package edu.washington.csep576;

import java.io.IOException;
import java.io.Writer;

import edu.washington.csep576.descriptors.AdvancedDescriptor;
import edu.washington.csep576.descriptors.Descriptor;
import edu.washington.csep576.descriptors.NonDescriptor;
import edu.washington.csep576.descriptors.SimpleDescriptor;

public class FeatureSet {

	Feature[] features;

	int type;

	private static String lineReturn = System.getProperty("line.separator");

	public FeatureSet(int type) {
		this.type = type;
	}

	public void write(Writer writer) throws IOException {
		writer.write(features.length + "");
		writer.write(lineReturn);
		int i = 1;
		for (Feature feature : features) {
			writer.write(type + "");
			writer.write(lineReturn);
			writer.write(i + "");
			i++;
			writer.write(lineReturn);
			feature.write(writer, type);
		}
	}

	private Descriptor getDescriptor() {
		switch (type) {
		case 0:
			return new NonDescriptor();
		case 1:
			return new SimpleDescriptor();
		case 2:
			return new AdvancedDescriptor();
		default:
			throw new RuntimeException("FeatureSet with type " + type
					+ " has no defined Feature Descriptor");
		}
	}
	
	public String getDescriptorName() {
		String classname = getDescriptor().getClass().getSimpleName();
		return classname.substring(0, classname.indexOf("Descriptor"));
	}

	public void describeFeatures(HarrisDetector harris, int[][] featureLocations) {
		features = new Feature[featureLocations.length];
		Descriptor descriptor = getDescriptor();
		for (int i = 0; i < features.length; i++) {
			int[] location = featureLocations[i];
			if (location.length != 2) {
				throw new RuntimeException("Got location element that was of size "
						+ location.length + " != 2");
			}
			Feature feature = descriptor.describeFeature(harris, location[0], location[1]);
			features[i] = feature;
		}
	}

	public static class Feature {
		// Location of this feature.
		int x, y;
		// Characteristics of this feature.
		double[] chars;

		public Feature(int x, int y) {
			this.x = x;
			this.y = y;
			chars = new double[0];
		}

		public Feature(int x, int y, double[] chars) {
			this(x, y);
			this.chars = chars;
		}

		private void write(Writer writer, int type) throws IOException {			
			writer.write(x + " " + y);
			writer.write(lineReturn);
			writer.write(chars.length + "");
			writer.write(lineReturn);
			for (Double c : chars) {
				writer.write(c.toString());
				writer.write(lineReturn);
			}
		}
	}
}
