package edu.washington.csep576;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.washington.csep576.FeatureSet.Feature;
import edu.washington.csep576.Matches.Match;
import edu.washington.csep576.images.PPM;

public class Analyzer {

	HarrisDetector baseHd;
	HarrisDetector otherHd;
	Matches matches;
	Homography homography;

	Analyzer(HarrisDetector baseHd, HarrisDetector otherHd, Matches matches,
			Homography homography) {
		this.baseHd = baseHd;
		this.otherHd = otherHd;
		this.matches = matches;
		this.homography = homography;
	}

	/**
	 * Runs three analyses. Each one produces an image representation of
	 * features matched. Some of them also produce stats that are passed back to
	 * be printed to system out (and included in the report.
	 * 
	 * @param outputFile
	 * @return
	 */
	public String run(File outputFile) {
		StringPPM sp;
		String ret = "";
		PPM[] ppms = new PPM[3];
		sp = firstAnalysis();
		ppms[0] = sp.ppm;
		ret += sp.s;
		ppms[1] = secondAnalysis().ppm;
		sp = thirdAnalysis();
		ret += sp.s;
		ppms[2] = sp.ppm;
		PPM mergedPPM = mergePPM(ppms);
		try {
			mergedPPM.write(new FileOutputStream(outputFile));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	/**
	 * A trivial little class for returning from our analyses.
	 */
	private static class StringPPM {
		String s;
		PPM ppm;
	}

	StringPPM firstAnalysis() {
		int features = matches.set2.features.length;
		int made = matches.matches.size();
		int inliers = 0;
		PPM ppm = otherHd.ppm;
		for (Feature feature : matches.set2.features) {
			drawSquare(ppm, RED, feature.x, feature.y, 3);
		}
		for (Match match : matches.matches) {
			double[] color = match.inlier ? GREEN : BLUE;
			inliers += match.inlier ? 1 : 0;
			int x = matches.set2.features[match.idx2].x;
			int y = matches.set2.features[match.idx2].y;
			drawSquare(ppm, color, x, y, 3);
			drawSquare(ppm, color, x, y, 4);
		}
		String ret = "";
		ret += features + " ";
		ret += made + " ";
		ret += inliers + " ";
		StringPPM sp = new StringPPM();
		sp.s = ret;
		sp.ppm = ppm;
		return sp;
	}

	StringPPM secondAnalysis() {
		PPM ppm = baseHd.ppm.copy();
		for (Match match : matches.matches) {
			double[] color = match.inlier ? GREEN : BLUE;
			int x = matches.set1.features[match.idx1].x;
			int y = matches.set1.features[match.idx1].y;
			drawSquare(ppm, color, x, y, 3);
			drawSquare(ppm, color, x, y, 4);
			int x1 = matches.set2.features[match.idx2].x;
			int y1 = matches.set2.features[match.idx2].y;
			drawLine(ppm, color, x, y, x1, y1);
			drawSquare(ppm, color, x1, y1, 1);
		}
		StringPPM sp = new StringPPM();
		sp.ppm = ppm;
		return sp;
	}

	StringPPM thirdAnalysis() {
		PPM ppm = baseHd.ppm.copy();
		double ssd = 0;
		int success = 0;
		for (Match match : matches.matches) {
			if (!match.inlier) {
				continue;
			}
			int x = matches.set1.features[match.idx1].x;
			int y = matches.set1.features[match.idx1].y;
			if(homography != null){
				drawSquare(ppm, YELLOW, x, y, 3);
			}
			drawSquare(ppm, GREEN, x, y, 4);
			int x1 = matches.set2.features[match.idx2].x;
			int y1 = matches.set2.features[match.idx2].y;
			if(homography != null){
				double[] siftCoords = homography.transform(x, y);
				int x2 = (int) Math.round(siftCoords[0]);
				int y2 = (int) Math.round(siftCoords[1]);
				drawLine(ppm, YELLOW, x, y, x2, y2);
				drawSquare(ppm, YELLOW, x2, y2, 1);
				double difs = Math.pow(x1 - siftCoords[0], 2) + Math.pow(y1 - siftCoords[1], 2);
				if(difs < 100){
					success++;
				}
				ssd += difs;
				
			}
			drawLine(ppm, GREEN, x, y, x1, y1);
			drawSquare(ppm, GREEN, x1, y1, 1);
		}
		ssd = Math.sqrt(ssd);
		StringPPM sp = new StringPPM();
		sp.ppm = ppm;
		sp.s = ssd + " " + success + " ";
		return sp;
	}

	static PPM drawFeatures(int[][] locations, PPM image) {
		PPM ret = image.copy();
		for (int[] location : locations) {
			drawSquare(ret, RED, location[0], location[1], 3);
		}
		return ret;
	}

	private static void drawSquare(PPM image, double[] color, int x, int y,
			int radius) {
		int x0 = x - radius;
		int x1 = x + radius;
		int y0 = y - radius;
		int y1 = y + radius;
		drawLine(image, color, x0, y0, x1, y0);
		drawLine(image, color, x0, y1, x1, y1);
		drawLine(image, color, x0, y0, x0, y1);
		drawLine(image, color, x1, y0, x1, y1);
	}

	private static void drawLine(PPM image, double[] color, int x0, int y0,
			int x1, int y1) {
		int steps = 2 * (Math.abs(x0 - x1) + Math.abs(y0 - y1) + 1);
		for (int step = 0; step <= steps; step++) {
			double ratio = step * 1d / steps;
			int x = Math.round((float) (x0 * ratio + x1 * (1 - ratio)));
			int y = Math.round((float) (y0 * ratio + y1 * (1 - ratio)));
			if(0 <= x && x < image.getWidth() && 0 <= y && y < image.getHeight()){
				image.setColor(x, y, color);
			}
		}
	}

	static final double[] RED   = new double[] { 255, 0, 0 };
	static final double[] YELLOW= new double[] { 255, 255, 0 };
	static final double[] BLUE  = new double[] { 0, 0, 255 };
	static final double[] GREEN = new double[] { 0, 255, 0 };
	static final double[] WHITE = new double[] { 255, 255, 255 };
	
	// We assume that the first image is the widest.
	private static PPM mergePPM(PPM[] ppms){
		int newHeight = (ppms.length - 1) * 10;
		for(PPM ppm : ppms){
			newHeight += ppm.getHeight();
		}
		PPM merged = ppms[0].copyExpand(newHeight);
		int i = 0;
		int start = 0;
		for(int y = 0; y < merged.getHeight(); y++){
			for(int x = 0; x < merged.getWidth(); x++){
				if(y - start < ppms[i].getHeight()){
					if(x < ppms[i].getWidth()){
						merged.setColor(x, y, ppms[i].getColor(x, y - start));
					} else {
						merged.setColor(x, y, WHITE);
					}
				} else {
					if(y - start - ppms[i].getHeight() < 10){
						merged.setColor(x, y, WHITE);
					} else {
						start = y;
						i = i+1;
						merged.setColor(x, y, ppms[i].getColor(x, y - start));
					}
				}
			}
		}
		return merged;
	}

}
