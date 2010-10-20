package edu.washington.csep576;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import edu.washington.csep576.images.PPM;

public class CSE576 {

	/**
	 * This does all the analysis of all the images. The sole argument is the
	 * parent directory of the image directories, which are expected to be named
	 * "bikes", "leuven", and "wall".
	 * 
	 * In addition, we require there to be a file named img1rot in each of these
	 * directories, containing a portion of a rotation of img1.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String parentDir = args[0];
		for (String imageCat : new String[] { "bikes", "leuven", "wall" }) {
			// The reference image file.
			File imageDir = new File(new File(parentDir), imageCat);
			File baseFile = new File(imageDir, "img1.ppm");
			// Read it in
			PPM basePPM = readPPM(baseFile);
			// Construct the feature detector
			HarrisDetector baseHd = new HarrisDetector();
			// Load image data
			baseHd.loadPPM(basePPM);
			// Compute the gradient vectors at each point
			baseHd.computeGradients();
			// Compute the corner response at each point
			baseHd.computeCornerResponse();
			// Choose local maxima, in an 11 x 11 region.
			int[][] baseFeatureLocations = baseHd.chooseFeatureLocations(5);
			// Draw features to "base" file if this is the first file
			PPM annotatedBasePPM = Analyzer.drawFeatures(baseFeatureLocations,
					basePPM);
			// Write out the base file for the report.
			writePPM(annotatedBasePPM, new File(new File(parentDir),
						imageCat + "Base.ppm"));
			for (int descriptor : new int[] { 2, 1 }) { // 1: Simple, 2:
														// Advanced
				FeatureSet baseFeatures = new FeatureSet(descriptor);
				// Describe the features
				baseFeatures.describeFeatures(baseHd, baseFeatureLocations);
				for (String other : new String[] { "2", "3", "4", "5", "6",
						"1rot" }) {
					// The next sequence of calls are the same as we did for the
					// base file, so we won't make the same comments here.
					File otherFile = new File(imageDir, "img" + other + ".ppm");
					PPM otherPPM = readPPM(otherFile);
					HarrisDetector otherHd = new HarrisDetector();
					otherHd.loadPPM(otherPPM);
					otherHd.computeGradients();
					otherHd.computeCornerResponse();
					int[][] otherFeatureLocations = otherHd.chooseFeatureLocations(5);
					FeatureSet otherFeatures = new FeatureSet(descriptor);
					otherFeatures.describeFeatures(otherHd, otherFeatureLocations);

					// Now we match baseFeatures with otherFeatures
					Matches matcher = new Matches(baseFeatures, otherFeatures);
					// Determine matches, using a threshold to eliminate false matches, and
					// denoting outliers based on pixel displacement.
					matcher.determineMatches();
					Homography homography = null;
					if(!other.equals("1rot")){
						homography = new Homography(new File(imageDir, "H1to" + other + "p"));
					}
					// The analyzer does the actual reporting.  Construct one that gives it everything
					// it needs to do so.
					Analyzer analyzer = new Analyzer(baseHd, otherHd, matcher, homography);
					// Run the analyzer.  It writes an image in parentDir with outputFilename, and
					// returns some data in the returned string.
					String desc = imageCat + "-" + baseFeatures.getDescriptorName() + "-1to" + other;
					String outputFilename = desc + ".ppm";
					File outputFile = new File(parentDir, outputFilename);
					String outputData = analyzer.run(outputFile);
					// We'll copy this out of the terminal to put in the report.
					System.out.println(desc + " " + outputData);
				}
			}
		}
	}

	// Little util to read PPM from given file and throw RTE if anything goes wrong
	static PPM readPPM(File f) {
		try {
			FileInputStream fis = new FileInputStream(f);
			return new PPM(fis);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	// Little util to write PPM to given file and throw RTE if anything goes wrong
	static void writePPM(PPM ppm, File f){
		try {
			ppm.write(new FileOutputStream(f));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	// Not using this anymore. Had used to look at features in CSEP576 UI before
	// I wrote my own analysis output code.
	static void writeFeatures(FeatureSet featureSet, String featuresetName) {
		try {
			// Strip of suffix, replace it with ".<featureSet>.f"
			// Assumes filename has a suffix appended with a "."
			String newFilename = featuresetName + "."
					+ featureSet.getDescriptorName() + ".f";
			BufferedWriter out = new BufferedWriter(new FileWriter(newFilename));
			featureSet.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
