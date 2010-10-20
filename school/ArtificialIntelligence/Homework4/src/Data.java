import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Data extends ArrayList<double[]> {

	private static final long serialVersionUID = 1L;

	public void readFile(String fileName) {
		try {
			FileReader input = new FileReader(fileName);
			BufferedReader bufRead = new BufferedReader(input);
			String line;
			while ((line = bufRead.readLine()) != null) {
				String[] parts = line.split("\t");
				double[] datum = new double[parts.length];
				for(int i = 0; i < parts.length; i++){
					try {
						datum[i] = Double.parseDouble(parts[i]);
					} catch (NumberFormatException e) {
						throw new RuntimeException(e);
					}
				}
				if(this.size() != 0 && datum.length != this.get(this.size() - 1).length){
					continue;				 
				}
				this.add(datum);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public double[] standardDev(double[] mean){
		double[] sigma = new double[mean.length];
		for(double[] datum : this){
			for(int i = 0; i < sigma.length; i++){
				sigma[i] += datum[i] * datum[i];
			}
		}
		for(int i = 0; i < sigma.length; i++){
			sigma[i] = Math.sqrt(sigma[i] / this.size());
		}
		return sigma;
	}
}
