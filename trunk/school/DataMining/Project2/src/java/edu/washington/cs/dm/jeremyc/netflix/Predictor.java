package edu.washington.cs.dm.jeremyc.netflix;

/**
 * The base class for all predictor implementations.
 */
public abstract class Predictor {

	/**
	 * The store through which all training data is accessed.
	 */
	protected MemStore memStore;

	public Predictor(MemStore storage) {
		this.memStore = storage;
	}

	/**
	 * Subclasses must implement this method. An array of predictions are
	 * returned (e.g. one for N nearest neighbor for N = 1 ... maxNumNeighbors)
	 * 
	 * @param userId
	 * @param movieId
	 */
	public abstract float[] predict(int userId, int movieId);

}