package edu.washington.cs.dm.jeremyc.netflix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.washington.cs.dm.jeremyc.netflix.Correlation.CountSort;
import edu.washington.cs.dm.jeremyc.netflix.Correlation.TopCorrelators;
import edu.washington.cs.dm.jeremyc.netflix.Correlation.WeightSort;
import edu.washington.cs.dm.jeremyc.netflix.MemStore.Rating;
import edu.washington.cs.dm.jeremyc.netflix.MemStore.Ratings;

/**
 * The basic implementation of nearest neighbor predictor, where either N
 * nearest users or N nearest movies may be used for calculating predicted
 * ratings.
 */
public class NNPredictor extends Predictor {

	/**
	 * The dimension along which this Nearest neighbors are calculated.
	 */
	EntityType type;

	/**
	 * The upper bound of the number of neighbors considered.
	 */
	int numNeighbors;

	public NNPredictor(MemStore storage, EntityType type, int numNeighbors) {
		super(storage);
		this.type = type;
		this.numNeighbors = numNeighbors;
	}

	@Override
	public float[] predict(int userId, int movieId) {
		TopCorrelators topCorrelators = getTopCorrelators(userId, movieId);
		return computeScore(topCorrelators, userId, movieId);
	}

	/**
	 * Determines and returns the numNeighbors nearest neighbors to the entity with the corresponding entityID
	 * @param userId
	 * @param movieId
	 * @return  A list sorted descending by nearness
	 */
	public TopCorrelators getTopCorrelators(int userId, int movieId) {
		List<Correlation> correlations = new ArrayList<Correlation>();
		for (Integer cId : memStore.getRelatedIds(type.dual(), type.dual()
				.select(userId, movieId))) {
			Correlation correlation = correlation(type, type.select(userId,
					movieId), cId);
			correlations.add(correlation);
		}
		int totalCount = 0;
		Collections.sort(correlations, new CountSort());
		TopCorrelators topCorrolators = new TopCorrelators(numNeighbors,
				new WeightSort());
		for (Correlation correlation : correlations) {
			totalCount += correlation.count;
			if (totalCount / correlations.size() < correlation.count) {
				topCorrolators.add(correlation);
			} else { break; }
		}
		return topCorrolators;

	}

	public Correlation correlation(EntityType type, int id2, int id1) {
		float numerator = 0f;
		float denominatorLeft = 0f;
		float denominatorRight = 0f;
		int count = 0;
		Ratings rs1 = memStore.getRatings(type, id1);
		Ratings rs2 = memStore.getRatings(type, id2);
		int i1 = 0;
		int i2 = 0;
		while (i1 < rs1.size() && i2 < rs2.size()) {
			Rating r1 = rs1.get(i1);
			Rating r2 = rs2.get(i2);
			if (r1.id == r2.id) {
				count++;
				float termLeft = r1.rating - rs1.ave;
				float termRight = r2.rating - rs2.ave;
				numerator += termLeft * termRight;
				denominatorLeft += termLeft * termLeft;
				denominatorRight += termRight * termRight;
				i1++;
				i2++;
			} else if (r1.id < r2.id) {
				i1++;
			} else {
				i2++;
			}
		}
		float denominator = (float) Math.sqrt(denominatorLeft
				* denominatorRight);
		float weight = 0;
		if (denominator == 0) {
			weight = 0;
		} else {
			weight = numerator / denominator;
		}
		return new Correlation(count, weight, type, id2, id1);
	}

	public float[] computeScore(Set<Correlation> topCorrolators, int userId,
			int movieId) {
		float[] scores = new float[numNeighbors];
		float score = 0;
		float denom = 0;
		float ave = memStore.getRatings(type, type.select(userId, movieId)).ave;
		int i = 0;
		for (Correlation c : topCorrolators) {
			int uid = type.select(c.entityId2, userId);
			int mid = type.select(movieId, c.entityId2);
			float otherAverage = memStore
					.getRatings(type, type.select(uid, mid)).ave;
			score += c.weight * (memStore.getRating(uid, mid) - otherAverage);
			denom += Math.abs(c.weight);
			scores[i++] = bound(ave + div(score, denom));
		}
		return scores;
	}

	private static float div(float score, float denom) {
		if (score == 0 || denom == 0) {
			return 0;
		} else
			return score / denom;
	}

	private static float bound(float score) {
		if (Float.isNaN(score))
			return 3;
		if (score > 5)
			return 5;
		if (score < 1)
			return 1;
		return score;
	}

}
