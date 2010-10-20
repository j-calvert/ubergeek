
public class Gaussian {

	double[] mean;

	double[] sigma; // Assuming the covariance matrix is diagonal.

	public Gaussian(int dimension){
		mean = new double[dimension];
		sigma = new double[dimension];
		for(int i = 0; i < dimension; i++){
			mean[i] = 0;
			sigma[i] = 0;
		}
	}
	
	public Gaussian(double[] mean, double[] sigma) {
		if (mean.length != sigma.length) {
			throw new RuntimeException(
					"Initializing Gaussian with inconsistent dimensions "
							+ mean.length + " and " + sigma.length);
		}
		this.mean = mean;
		this.sigma = sigma;
	}

	public boolean withinSigma(double[] datapoint) {
		if (datapoint.length != mean.length) {
			throw new RuntimeException(
					"Testing Gaussian with inconsistent dimensions "
							+ datapoint.length + " and " + sigma.length);
		}
		for (int i = 0; i < datapoint.length; i++) {
			if (Math.abs(datapoint[i] - mean[i]) > sigma[i]) {
				return false;
			}
		}
		return true;
	}

	public double probability(double[] point) {
		if (point.length != mean.length) {
			throw new RuntimeException(
					"Finding Gaussian probability with inconsistent dimensions "
							+ point.length + " and " + sigma.length);
		}
		double exponent = 0d;
		double coefficient = 1d;
		for (int i = 0; i < point.length; i++) {
			exponent += (point[i] - mean[i]) * (point[i] - mean[i]) / sigma[i];
			coefficient *= sigma[i];
		}
		exponent = -exponent / 2;
		coefficient *= Math.pow(2 * Math.PI, sigma.length);
		coefficient = Math.sqrt(coefficient);
		return Math.exp(exponent) / coefficient;
	}
		
	@Override
	public String toString(){
		return "Mean: " + a2s(mean) + " Sigma: " + a2s(sigma);
	}
	
	private static String a2s(double[] ds){
		String s = "[";
		for(double d : ds){
			s += d + ", ";
		}
		return s + "]";
	}
	
	public double[] distances(Gaussian g){
		double[] distances = {0, 0};
		for(int i = 0; i < g.mean.length; i++){
			distances[0] += Math.pow(this.mean[i] - g.mean[i], 2); 
			distances[1] += Math.pow(this.sigma[i] - g.sigma[i], 2); 
		}
		distances[0] = Math.sqrt(distances[0]);
		distances[1] = Math.sqrt(distances[1]);
		return distances;
	}

}
