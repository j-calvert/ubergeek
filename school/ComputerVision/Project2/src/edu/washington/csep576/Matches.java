package edu.washington.csep576;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.washington.csep576.FeatureSet.Feature;


public class Matches {
	
	FeatureSet set1;
	FeatureSet set2;
	List<Match> matches;

	// Takes 2 features sets, performs matching.
	Matches(FeatureSet... sets) {
		// Ensure we're looking at a pair
		if(sets.length != 2){
			throw new RuntimeException("Can only match pairs of feature sets");
		}
		this.set1 = sets[0];
		this.set2 = sets[1];
		// Ensure that the two sets have the same feature type
		if (set1.type != set2.type) {
			throw new RuntimeException(
					"Comparing apples and oarnges (feature sets of different types)");
		}
		matches = new ArrayList<Match>();
	}
	
	void determineMatches(){
		for(int i2 = 0; i2 < set2.features.length; i2++){
			Feature f2 = set2.features[i2];
			double closest = Double.MAX_VALUE;
			double nextClosest = closest;
			int clIdx = 0;
			for(int i1 = 0; i1 < set1.features.length; i1++){
				double dist = featureDistance(set1.features[i1], f2);
				if(dist < closest){
					nextClosest = closest;
					closest = dist;
					clIdx = i1;
				}
			}
			// Threshold as described in section 7.1 of 
			// the sift paper.
			if(closest / nextClosest < 0.8){
				matches.add(new Match(clIdx, i2));
			}
		}
		markOutliers();
	}
	
	/**
	 * Mark outliers based on much larger than normal pixel distance
	 * This won't work very well for rotations or scalings where there's non-uniform
	 * displacement lengths
	 */
	private void markOutliers() {
		List<Double> distances = new ArrayList<Double>();
		for(Match match : matches){
			distances.add(pixelDistance(set1.features[match.idx1],set2.features[match.idx2]));
		}
		Collections.sort(distances);
		// Take twice the distance between the 95th percentile pair
		int tenth = Math.min(distances.size() / 20, distances.size() - 1);
		double threshold = 2 * distances.get(tenth);
		for(Match match : matches){
			if(threshold < pixelDistance(set1.features[match.idx1],set2.features[match.idx2])){
				match.inlier = false;
			} else {
				match.inlier = true;
			}
		}
		
	}
	
	private double pixelDistance(Feature f1, Feature f2){
		double dist = Math.pow(f1.x - f2.x, 2);
		dist += Math.pow(f1.y - f2.y, 2);
		return Math.sqrt(dist);		
	}
	
	private double featureDistance(Feature f1, Feature f2){
		double dist = 0;
		for(int i = 0; i < f1.chars.length; i++){
			dist += Math.pow(f1.chars[i] - f2.chars[i], 2);
		}
		return Math.sqrt(dist);
	}
	
	static class Match {
		int idx1; // The index of feature being matched from set1
		int idx2; // The index of feature being matched from set2
		boolean inlier = false;
		
		Match(int idx1, int idx2){
			this.idx1 = idx1;
			this.idx2 = idx2;			
		}
	}
}
