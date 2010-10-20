package edu.washington.cs.dm.jeremyc.netflix;

import java.util.HashMap;
import java.util.Map;

import edu.washington.cs.dm.jeremyc.netflix.Correlation.TopCorrelators;

/**
 * Uses a weighted average of Nearest Neighbor predictions along both dimensions
 * (movie and user).
 */
public class DualNNPredictor extends Predictor {

	/**
	 * A map of dimension to predictor.
	 */
	private Map<EntityType, NNPredictor> predictors;

	/**
	 * The max number of nearest neighbors to consider in NN calculations.
	 */
	private int numNeighbors;

	public DualNNPredictor(MemStore storage, int numNeighbors) {
		super(storage);
		predictors = new HashMap<EntityType, NNPredictor>();
		for (EntityType type : EntityType.values()) {
			predictors.put(type, new NNPredictor(storage, type, numNeighbors));
		}
		this.numNeighbors = numNeighbors;
	}

	/**
	 * Implementation of the predict method specified in the base class. Does NN
	 * calculations i both dimensions and then takes a weighted average.
	 */
	@Override
	public float[] predict(int userId, int movieId) {
		float[] netWeights = new float[numNeighbors];
		float[] aveScores = new float[numNeighbors];
		for (EntityType type : EntityType.values()) {
			TopCorrelators topCorrolators = predictors.get(type)
					.getTopCorrelators(userId, movieId);
			float[] thisScores = predictors.get(type).computeScore(
					topCorrolators, userId, movieId);
			float[] thisWeights = topCorrolators.netWeights(numNeighbors);
			for (int i = 0; i < aveScores.length; i++) {
				aveScores[i] += thisScores[i] * thisWeights[i];
				netWeights[i] += thisWeights[i];
			}
		}
		for (int i = 0; i < aveScores.length; i++) {
			aveScores[i] = aveScores[i] / netWeights[i];
			if (Float.isNaN(aveScores[i])) {
				aveScores[i] = 3;
			}
		}
		return aveScores;
	}
}
