package edu.washington.cs.dm.jeremyc.netflix;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * A simple container for a co-occurence count, rating, entity type and entity
 * IDs. When there is an implicit entity to which this is the correlation, the
 * id of the implicit entity is store in entityId1.
 */
public class Correlation {
	/**
	 * The number of cooccurences.
	 */
	public int count;

	/**
	 * The correlation according to equation 2.1 in the paper cited in the
	 * project.
	 */
	public float weight;

	/**
	 * Whether the entityIDs correspond to movies or users.
	 */
	public EntityType type;

	/**
	 * The first ID (if dealing with a collection of Correlations, this will be
	 * the same in all instances...really don't need to store this (started to
	 * when I thought it would be worthwhile to cache Correlations)
	 */
	public int entityId1;

	/**
	 * The second ID, should be different for each instance of a Correlation in
	 * a collection.
	 */
	public int entityId2;

	public Correlation(int count, float weight, EntityType type, int entityId,
			int entityId2) {
		this.count = count;
		this.weight = weight;
		this.type = type;
		this.entityId1 = entityId;
		this.entityId2 = entityId2;
	}

	public String toString() {
		return count + ", " + weight + ", " + type + ", " + entityId1 + ", "
				+ entityId2 + ";";
	}

	/**
	 * An ordered (by nearness) set of Correlations.
	 */
	public static class TopCorrelators extends TreeSet<Correlation> {

		private static final long serialVersionUID = 1L;

		/**
		 * The name number of nearest neighbors we need to store (we calculate
		 * this once for NN computation for N = 1 ... capacity.)
		 */
		private int capacity;

		/**
		 * The constructor, which takes a comparator that determines nearness in
		 * the nearest neighbor qualification.
		 * 
		 * @param capacity
		 * @param comp
		 */
		public TopCorrelators(int capacity, Comparator<Correlation> comp) {
			super(comp);
			this.capacity = capacity;
		}

		/**
		 * Overrides the ordered set method to truncate members that are not
		 * within the top-capacity.
		 */
		@Override
		public boolean add(Correlation o) {
			boolean ret = super.add(o);
			while (size() > capacity) {
				remove(last());
			}
			return ret;
		}

		/**
		 * Returns an array of net weights, used in caculating the normalizing
		 * factor when predicting the score.
		 * 
		 * @param size
		 */
		public float[] netWeights(int size) {
			float[] weights = new float[size];
			float weight = 0f;
			int i = 0;
			for (Correlation c : this) {
				weight += Math.abs(c.weight);
				weights[i++] = weight;
			}
			return weights;
		}
	}

	/**
	 * Sorts Correlations according to "nearness"
	 */
	static class WeightSort implements Comparator<Correlation> {

		public int compare(Correlation o1, Correlation o2) {
			if (Math.abs(o1.weight) > Math.abs(o2.weight)) {
				return -1;
			} else if (o1.equals(o2)) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	/**
	 * Used to pre-sort the correlations when applying a dynamic,
	 * cooccurence count threshold to the neighbors (we've used it to insist that
	 * the cooccurence is at least as large as the running average of cooccurences
	 * thus far, when sorting by descending cooccurences).
	 */
	static class CountSort implements Comparator<Correlation> {

		WeightSort weightSort = new WeightSort();

		public int compare(Correlation o1, Correlation o2) {
			if (o1.count > o2.count) {
				return -1;
			} else if (o1.count < o2.count){
				return 1;
			} else {
				return weightSort.compare(o1, o2);
			}
		}

	}
}

