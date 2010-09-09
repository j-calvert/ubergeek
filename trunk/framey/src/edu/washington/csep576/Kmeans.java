package edu.washington.csep576;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Kmeans {

	// clusterId --> Color. Use a map (as opposed to an array) in order to track
	// clusterID presence with key presence. Store color in 4x less efficient
	// structure, it's simpler, and there's on a few instances of them. Here,
	// the A element holds the number of pixels in the cluster.
	private Map<Byte, RGB> clusters = new HashMap<Byte, RGB>();
	// pixel index --> clusterId
	private byte[] clusterAlloc;

	// the image to be converted into cluster masks
	private final int[] pixels;
	private final int width, height;

	// used to merge similar clusters
	private double mergeFactor = 0;
	// the maximum number of clusters left when algorithm is finished.
	private int maxClusters = Integer.MAX_VALUE;

	public Kmeans(Image image, double mergeFactor, int maxClusters) {
		this.width = image.getWidth(null);
		this.height = image.getHeight(null);
		pixels = new int[width * height];
		clusterAlloc = new byte[pixels.length];

		PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		this.mergeFactor = mergeFactor;
		this.maxClusters = maxClusters;
	}

	public void cluster() {
		selectSeeds(maxClusters);
		if (clusters == null || clusters.isEmpty()) {
			throw new RuntimeException(
					"Must seed clusters before running clusterColors");
		}
		int changes;
		int totalChanges = 0;
		int iterations = 0;
		do {
			iterations++;
			changes = 0;
			for (int i = 0; i < pixels.length; i++) {
				byte c = closestCluster(pixels[i]);
				if (c != clusterAlloc[i]) {
					changes++;
					clusterAlloc[i] = c;
				}
			}
			System.err.println("Pixels changing cluster: " + changes);
			if (changes == 0
					|| changes * mergeFactor < totalChanges / iterations) {
				mergeFactor *= 1.01;
				System.err.println("Adjusted merge factor to " + mergeFactor);
			}
			changes += refineClusters();
			totalChanges += iterations > 3 ? changes : 0;
		} while (changes > 0 || maxClusters < clusters.size());
	}

	
	
	
	private byte closestCluster(int clr) {
		byte ret = 0;
		double min = Double.MAX_VALUE;
		for (Entry<Byte, RGB> ic : clusters.entrySet()) {
			double d = ic.getValue().distance(clr);
			if (d < min) {
				min = d;
				ret = ic.getKey();
			}
		}
		return ret;
	}

	/**
	 * @return The number of pixels that have changed clusters as a result of
	 *         refinement.
	 */
	private int refineClusters() {
		int changes = 0;
		Map<Integer, RGB> clusterSums = new HashMap<Integer, RGB>();
		for (int i = 0; i < pixels.length; i++) {
			int clusterId = clusterAlloc[i];
			if (!clusterSums.containsKey(clusterId)) {
				clusterSums.put(clusterId, new RGB(pixels[i], 1));
			} else {
				clusterSums.get(clusterId).add(new RGB(pixels[i], 1));
			}
		}
		for (Iterator<Byte> it = clusters.keySet().iterator(); it.hasNext();) {
			Byte b = it.next();
			if (!clusterSums.containsKey(b)) {
				clusters.remove(b);
			}
		}
		for (Byte k : clusters.keySet()) {
			clusters.put(k, clusterSums.get(k).getDivByA());
		}

		// /////// Fancier calculation of K .... Merge clusters if their centers
		// get close to one another ///////
		if (mergeFactor > 0 && maxClusters < clusters.size()) {
			Map<Integer, Float> clusterDiams = new HashMap<Integer, Float>();
			// Sum distances from mean in cluster
			for (int i = 0; i < pixels.length; i++) {
				int clusterId = clusterAlloc[i];
				if (!clusterDiams.containsKey(clusterId)) {
					clusterDiams.put(clusterId, 0f);
				}
				clusterDiams.put(clusterId, clusterDiams.get(clusterId)
						+ clusters.get(clusterId).distance(pixels[i]));
			}

			// Now normalize by size of cluster
			for (int clusterId : clusterDiams.keySet()) {
				clusterDiams.put(clusterId, clusterDiams.get(clusterId)
						/ clusterSums.get(clusterId).getA());
			}

			// Now see if if any other cluster's centers are within mergeFactor
			// of the center of any other, and if they are, merge them.
			byte[] ij = minDistClusters();
			byte i = ij[0];
			byte j = ij[1];
			float diamSum = clusterDiams.get(i) + clusterDiams.get(j);
			float dist = clusters.get(i).distance(clusters.get(j));
			if (dist < diamSum / 2 * mergeFactor) {
				System.err.println("Merging clusters " + i + " and " + j);
				mergeFactor = 2 * dist / diamSum * .99;
				System.err.println("Setting mergeFactor to " + mergeFactor);
				clusters.get(i).weightAve(clusters.get(j));
				clusters.remove(j);
				for (int k = 0; k < pixels.length; k++) {
					if (clusterAlloc[k] == j) {
						clusterAlloc[k] = i;
						changes++;
					}
				}
			}
		}
		return changes;
	}

	/**
	 * Find the pair of clusters that are closest to one another, and return
	 * their indices
	 * 
	 * @return pair of ints (bytes). So happens, b[0] < b[1]
	 */
	private byte[] minDistClusters() {
		float min = Float.MAX_VALUE;
		byte[] ret = new byte[2];
		for (Entry<Byte, RGB> e0 : clusters.entrySet()) {
			for (Entry<Byte, RGB> e1 : clusters.entrySet()) {
				if (e0.getKey() >= e1.getKey()) {
					continue;
				}
				float thisMin = e0.getValue().distance(e1.getValue());
				if (thisMin < min) {
					min = thisMin;
					ret[0] = e0.getKey();
					ret[1] = e1.getKey();
				}
			}
		}
		return ret;
	}

	/**
	 * Select seeds for the centers of clusters optimizing for distance of seed
	 * colors from one another.
	 */
	private void selectSeeds(int k) {
		double thresholdDifference = Math.pow(3, .33);
		byte nextIdx = 0;
		clusters.put(nextIdx, RGB.random());
		while (nextIdx < k) {
			thresholdDifference += -0.0001d;
			RGB candidate = RGB.random();
			boolean far = true;
			for (RGB selected : clusters.values()) {
				if (selected.distance(candidate) < thresholdDifference) {
					far = false;
					break;
				}
			}
			if (far) {
				System.err.println("Added seed at distance " + thresholdDifference);
				clusters.put(nextIdx, candidate);
				nextIdx++;
			}
		}
	}
}
