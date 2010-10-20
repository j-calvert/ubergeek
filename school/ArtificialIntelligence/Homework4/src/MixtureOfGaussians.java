import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MixtureOfGaussians {

	List<Gaussian> gaussians = new ArrayList<Gaussian>();	
	Data data;
	Random random = new Random();
	double[][] prob;
	double epsilon = Double.MIN_VALUE * Math.pow(Math.E, 100);
	double convergence = Math.pow(10, -10);
	
	private static final String[] clusterNames 
		= {"Iris-setosa", "Iris-versicolor", "Iris-virginica"};
	
	public MixtureOfGaussians(String dataFile, int numGaussians){
		data = new Data();
		data.readFile(dataFile);
		for(int i = 0; i < numGaussians; i++){
			gaussians.add(newGaussian(numGaussians));
		}
		prob = new double[numGaussians][data.size()];
		for(int i = 0; i < prob.length; i++){
			for(int j = 0; j < prob[i].length; j++){
				prob[i][j] = 1d / prob.length;
			}
		}
	}
	
	public void doEMClustering() {
		double divergence = Double.MAX_VALUE;
		while(divergence > convergence){
			Estep();
			divergence = Mstep();
			System.out.println(divergence);
		}
	}
	
	public void Estep(){
		double[] weights = getWeights();
		for(int j = 0; j < prob[0].length; j++){
			double totalProb = 0;
			for(int i = 0; i < prob.length; i++){
				prob[i][j] = gaussians.get(i).probability(data.get(j)) * weights[i];
				totalProb += prob[i][j];
			}
			// normalize probs for data element j
			for(int i = 0; i < prob.length; i++){
				prob[i][j] = prob[i][j] / totalProb;
			}
		}
	}
	
	public double Mstep() {
		double[] weights = getWeights();
		double netDivergence = 0;
		for(int i = 0; i < gaussians.size(); i++){
			Gaussian g = gaussians.get(i);
			Gaussian gNew = new Gaussian(g.mean.length);
			for(int j = 0; j < prob[i].length; j++){
				double[] datum = data.get(j);
				for(int k = 0; k < g.mean.length; k++){
					gNew.mean[k] += prob[i][j] * datum[k]/ weights[i];
					gNew.sigma[k] += prob[i][j] * (datum[k] - g.mean[k]) * (datum[k] - g.mean[k]) / weights[i];
				}
			}
			double[] distances = g.distances(gNew);
			netDivergence += distances[0] + distances[1];
			gaussians.set(i, gNew);
		}
		
		for(Gaussian g : gaussians){
			double det = 1;
			for(double s : g.sigma){
				det *= s;
			}
			if(det < epsilon){
				System.err.println("Got too small a determinant for gaussian: " + g);
			}
		}
		return netDivergence;
	}

	private double[] getWeights(){
		double[] weights = new double[prob.length];
		for(int i = 0; i < weights.length; i++){
			weights[i] = 0;
			for(int j = 0; j < prob[i].length; j++){
				weights[i] += prob[i][j];
			}
		}
		return weights;
	}
	
	/**
	 * Creates a new gaussian with mean equal to a randomly chosen datum, and standard deviation equal to
	 * S/(number of gaussians) where S is the standard deviation with the whole dataset.
	 * 
	 * It chooses a point that's not within a standard deviation of any existing gaussians.
	 * @return
	 */
	private Gaussian newGaussian(int numGaussians){
		double[] candidate;
		do{
			candidate = data.get(random.nextInt(data.size()));
		}while(tooClose(candidate));
		double[] sigma = data.standardDev(candidate);
		for(int i = 0; i < sigma.length ; i++){
			sigma[i] = sigma[i] / numGaussians;
		}
		return new Gaussian(candidate, sigma);
	}
	
	private boolean tooClose(double[] candidate){
		for(Gaussian gaussian : gaussians){
			if(gaussian.withinSigma(candidate)){
				return true;
			}
		}
		return false;
	}
	
	private Map<Integer, String> mapGaussiansToClusternames(){
		Map<String, int[]> map = new HashMap<String, int[]>();
		for(String c : clusterNames){
			map.put(c, new int[]{0, 0, 0});
		}
		for(int i = 0; i < data.size(); i++){
			map.get(trueCluster(i))[placeInCluster(data.get(i))]++;
		}
		Map<Integer, String> ret = new HashMap<Integer, String>();
		for(String c : clusterNames){
			ret.put(maxIndex(map.get(c)), c);
		}
		return ret;
	}
	
	private static int maxIndex(int[] ints){
		int maxIndex = 0;
		for(int i = 0; i < ints.length; i++){
			if(ints[i] > ints[maxIndex]){
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	private int placeInCluster(double[] datum){
		double maxProb = 0;
		int winner = -1;
		for(int j = 0; j < gaussians.size(); j++){
			double prob = gaussians.get(j).probability(datum);
			if(prob > maxProb){
				maxProb = prob;
				winner = j;
			}
		}		
		return winner;
	}
	
	private static String trueCluster(int row){
		return clusterNames[row / 50];
	}
	
	private void printResults(String filename) throws Exception {
		   BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		Map<Integer, String> nameMap = mapGaussiansToClusternames();
		for(int i = 0; i < data.size(); i++) {
			double[] datum = data.get(i);
			for(int j = 0; j < datum.length; j++){
				out.write(datum[j] + ",");				
			}
			out.write(trueCluster(i) + ",");
			out.write(nameMap.get(placeInCluster(datum)));
			out.write(System.getProperty("line.separator"));
		}
		out.close();
	}
	
	public static void main(String[] args) {
		try {
			MixtureOfGaussians mog = new MixtureOfGaussians(args[0], 3);
			mog.doEMClustering();
			mog.printResults(args[1]);
		} catch (Exception e) {
			error(e);
		}
	}
	
	private static void error(Exception e){
		System.err.println("Usage: java MixtureOfGaussians inputFile outputFile");
		e.printStackTrace();
	}
	
}
