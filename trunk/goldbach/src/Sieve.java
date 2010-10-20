
import java.util.ArrayList;
import java.util.List;

public class Sieve extends ArrayList<Integer> {
	private static final long serialVersionUID = 1L;

	public Sieve(int maxPrime){
		super();
		for(int i = 2; i <= maxPrime; i++){
			add(i);
		}
		int j = 0;
		while(j < size()){
			int k = 2;
			int p = get(j);
			while(p * k <= maxPrime){
				remove(Integer.valueOf(p * k));
				k++;
			}
			j++;
		}
	}
	
	public List<Integer> getFactors(int number){
		List<Integer> factors = new ArrayList<Integer>();
		if(get(size() - 1) * 2 < number) {
			throw new RuntimeException("This sieve isn't big enough");
		}
		int i = 0;
		while(i < size() && get(i) <= number){
			while(number % get(i) == 0){
				factors.add(get(i));
				number = number / get(i);
			}
			i++;
		}
		if(number != 1){
			throw new RuntimeException("Your math sucks");
		}
		return factors;
	}
	
	public static void main(String[] args){
		Sieve p = new Sieve(1000);
		System.out.println(p.toString());
		System.out.println(p.getFactors(92136));
	}

}
