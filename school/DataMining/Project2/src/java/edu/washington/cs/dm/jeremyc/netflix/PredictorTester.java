package edu.washington.cs.dm.jeremyc.netflix;

import java.io.BufferedReader;

import edu.washington.cs.dm.jeremyc.netflix.Populator.DataRow;

/**
 * The main method for testing a predictor. Change the derived instance of
 * Predictor passed to the Predictor harness to test a given implementation.
 */
public class PredictorTester {

	/**
	 * Instantiates and populates a MemStore using a Populator.
	 * 
	 * @param args
	 *            [(trainingFile), (testingFile), (NumNeighbor_range)] where a
	 *            NN calculation is done for N = 1 ... NumNeighbor_range
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			usage();
		}
		try {
			int numNeighbors = Integer.parseInt(args[2]);
			MemStore storage = new Populator().getPopulatedStore(args[0]);
			PredictorHarness predictorHarness = new PredictorHarness(
					new DualNNPredictor(storage, numNeighbors),
					numNeighbors);
			BufferedReader in = Populator.readFile(args[1]);
			DataRow test = null;
			String line = null;
			while ((line = in.readLine()) != null) {
				test = Populator.parseLine(line);
				testPredictor(test, predictorHarness);
			}
			System.out.println("Final results:");
			System.out.println(predictorHarness);
		} catch (RuntimeException e) {
			e.printStackTrace();
			usage();
		}
	}

	/**
	 * Tests the predictor held in the predictor harness for the given row of
	 * data.
	 * 
	 * @param data
	 *            Contains movieId, userId, and actual rating.
	 * @param predictorHarness
	 */
	public static void testPredictor(DataRow data,
			PredictorHarness predictorHarness) {
		float[] predictions = predictorHarness.predictor.predict(data.userId,
				data.movieId);
		predictorHarness.numPredictions++;
		for (int i = 0; i < predictions.length; i++) {
			float error = data.rating - predictions[i];
			predictorHarness.squareErrors[i] += error * error;
			predictorHarness.averageErrors[i] += Math.abs(error);
			if (Float.isNaN(error) || Float.isInfinite(error)) {
				predictorHarness.predictor.predict(data.userId, data.movieId);
			}
		}
		if (predictorHarness.numPredictions % 100 == 0) {
			System.out.println(predictorHarness);
		}
	}

	/**
	 * Produces an error statement if command line parameters were insufficient.
	 */
	private static void usage() {
		System.err
				.println("Usage: run.sh <trainingFile> <testingFile> <num nearest>");
		System.err
				.println("where filename is the name of the testing data file (e.g. \"TestingRatings.txt\")");
		System.exit(1);
	}

	/**
	 * A simple container class.
	 */
	static class PredictorHarness {
		/**
		 * The Predictor being tested.
		 */
		Predictor predictor;

		/**
		 * The sum of the square of the errors for the nearest neighbor
		 * calculations (one for each value of N in N nearest neighbor, N = 1
		 * ... numNeighbors)
		 */
		float[] squareErrors;

		/**
		 * The sum of the absolute value of the errors for the nearest neighbor calculations (one
		 * for each value of N in N nearest neighbor, N = 1 ... numNeighbors)
		 */
		float[] averageErrors;

		/**
		 * The total number of predictions (used to find mean).
		 */
		int numPredictions = 0;

		PredictorHarness(Predictor predictor, int numPredictions) {
			this.predictor = predictor;
			squareErrors = new float[numPredictions];
			averageErrors = new float[numPredictions];
		}

		public String toString() {
			String ret = "MAE@" + numPredictions + ":";
			for (int i = 0; i < averageErrors.length; i++) {
				ret += " " + to4dec(averageErrors[i] / numPredictions);
			}
			ret += "\nRMSE@" + numPredictions + ":";
			for (int i = 0; i < squareErrors.length; i++) {
				ret += " "
						+ to4dec((float) Math.sqrt(squareErrors[i]
								/ numPredictions));
			}
			return ret;
		}

		private static float to4dec(float f) {
			return ((float) Math.round(10000 * f) / 10000);

		}
	}
}
