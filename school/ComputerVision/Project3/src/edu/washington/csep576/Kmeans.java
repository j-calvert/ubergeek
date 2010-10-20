package edu.washington.csep576;

import java.util.Random;

public class Kmeans {

	private String name;
	// clusterId --> Color
	private double[][] clusters;
	// (x, y) --> clusterId
	private int[][] clusterAlloc;
	// the image to be converted into cluster masks
	private PPM ppm;
	// used to merge similar clusters
	private double mergeFactor = 0;
	// the maximum number of clusters left when algorithm is finished.
	private int maxClusters = Integer.MAX_VALUE;

	public Kmeans(){}
	
	public Kmeans(String name, PPM ppm, double mergeFactor, int maxClusters) {
		this.name = name;
		this.ppm = ppm.copy();
		this.mergeFactor = mergeFactor;
		this.maxClusters = maxClusters;
		selectSeeds(maxClusters);
		clusterColors();
		doMask();
	}

	public PPM getPPM() {
		return ppm;
	}

	public ConCompClusters makeCCClusters(){
		return new ConCompClusters(name, clusters, clusterAlloc);
	}
	
	private void clusterColors() {
		if (clusters == null || clusters.length == 0) {
			throw new RuntimeException(
					"Must seed clusters before running clusterColors");
		}
		int changes;
		int totalChanges = 0;
		int iterations = 0;
		do {
			iterations++;
			changes = 0;
			for (int x = 0; x < clusterAlloc.length; x++) {
				for (int y = 0; y < clusterAlloc[x].length; y++) {
					int j = closestCluster(ppm.getColor(x, y));
					if (j != clusterAlloc[x][y]) {
						changes++;
						clusterAlloc[x][y] = j;
					}
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
		} while (changes > 0 || maxClusters < clusters.length);
	}

	private int closestCluster(double[] colors) {
		int ret = 0;
		double min = Double.MAX_VALUE;
		for (int i = 0; i < clusters.length; i++) {
			double d = Util.distance(clusters[i], colors);
			if (d < min) {
				min = d;
				ret = i;
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
		double[][] clusterSums = new double[clusters.length][4];
		for (int x = 0; x < clusterAlloc.length; x++) {
			for (int y = 0; y < clusterAlloc[x].length; y++) {
				for (int c = 0; c < 3; c++) {
					clusterSums[clusterAlloc[x][y]][c] += ppm.getColor(x, y)[c];
				}
				clusterSums[clusterAlloc[x][y]][3]++;
			}
		}
		for (int i = 0; i < clusters.length; i++) {
			if (clusterSums[i][3] == 0) {
				System.err.println("Removing empty cluster " + i);
				clusters = removeElement(i, clusters);
				clusterSums = removeElement(i, clusterSums);
				i--;
			} else {
				for (int c = 0; c < 3; c++) {
					clusters[i][c] = clusterSums[i][c] / clusterSums[i][3];
				}
			}
		}
		// /////// Fancier calculation of K .... Merge clusters if their centers
		// get close to one another ///////
		if (mergeFactor > 0 && maxClusters < clusters.length) {
			double[] clusterDiams = new double[clusters.length];
			// Sum distances from mean in cluster
			for (int x = 0; x < clusterAlloc.length; x++) {
				for (int y = 0; y < clusterAlloc[x].length; y++) {
					clusterDiams[clusterAlloc[x][y]] += Util.distance(
							clusters[clusterAlloc[x][y]], ppm.getColor(x, y));
				}
			}
			// Now normalize by size of cluster
			for (int i = 0; i < clusterDiams.length; i++) {
				clusterDiams[i] = clusterDiams[i] / clusterSums[i][3];
			}
			// Now see if if any other cluster's centers are within mergeFactor
			// of the center of any other, and if they are, merge them.
			int[] ij = minDistClusters();
			int i = ij[0];
			int j = ij[1];
			if (Util.distance(clusters[i], clusters[j]) < (clusterDiams[i] + clusterDiams[j])
					/ 2 * mergeFactor) {
				System.err.println("Merging clusters " + i + " and " + j);
				mergeFactor = 2 * Util.distance(clusters[i], clusters[j])
						/ (clusterDiams[i] + clusterDiams[j]) * .99;
				System.err.println("Setting mergeFactor to " + mergeFactor);
				clusters[i] = Util.ave(clusters[i], clusters[j]);
				clusterDiams[i] = (clusterSums[i][3] * clusterDiams[i] + clusterSums[j][3]
						* clusterDiams[j])
						/ (clusterSums[i][3] + clusterSums[j][3]);
				clusterSums[i] = Util.sum(clusterSums[i], clusterSums[j]);
				for (int x = 0; x < clusterAlloc.length; x++) {
					for (int y = 0; y < clusterAlloc[x].length; y++) {
						if (clusterAlloc[x][y] == j) {
							clusterAlloc[x][y] = i;
							changes++;
						}
					}
				}
				clusters = removeElement(j, clusters);
			}
		}
		return changes;
	}

	/**
	 * Find the pair of clusters that are closest to one another, and return
	 * their indices
	 * 
	 * @return
	 */
	private int[] minDistClusters() {
		double d = Double.MAX_VALUE;
		int[] ret = new int[2];
		for (int i = 0; i < clusters.length; i++) {
			for (int j = i + 1; j < clusters.length; j++) {
				if (Util.distance(clusters[i], clusters[j]) < d) {
					ret[0] = i;
					ret[1] = j;
					d = Util.distance(clusters[i], clusters[j]);
				}
			}
		}
		return ret;
	}

	/**
	 * Little utility to remove an element from a statically sized array of
	 * arrays.
	 */
	private double[][] removeElement(int i, double[][] orig) {
		double[][] newArray = new double[orig.length - 1][orig[0].length];
		int k = 0;
		for (int j = 0; j < orig.length; j++) {
			if (i != j) {
				newArray[k] = orig[j];
				k++;
			}
		}
		return newArray;
	}

	/**
	 * Replace each pixel with the mean color for its cluster
	 */
	private void doMask() {
		for (int x = 0; x < ppm.getWidth(); x++) {
			for (int y = 0; y < ppm.getHeight(); y++) {
				ppm.setColor(x, y, clusters[clusterAlloc[x][y]]);
			}
		}
	}

	/**
	 * Select seeds for the centers of clusters optimizing for distance of seed
	 * colors from one another.
	 */
	private void selectSeeds(int k) {
		Random r = new Random();
		clusters = new double[k][3];
		clusterAlloc = new int[ppm.getWidth()][ppm.getHeight()];
		double thresholdDifference = Math.pow(3, .33);
		clusters[0] = random3(r);
		int nextIdx = 1;
		while (nextIdx < k) {
			thresholdDifference += -0.0001d;
			double[] candidate = random3(r);
			boolean far = true;
			for (int i = 0; i < nextIdx; i++) {
				if (Util.distance(clusters[i], candidate) < thresholdDifference) {
					far = false;
					break;
				}
			}
			if (far) {
				System.err.println("Added seed at distance "
						+ thresholdDifference);
				clusters[nextIdx] = candidate;
				nextIdx++;
			}
		}
	}

	private double[] random3(Random r) {
		double[] color = new double[3];
		color[0] = r.nextFloat();
		color[1] = r.nextFloat();
		color[2] = r.nextFloat();
		return color;
	}

}
