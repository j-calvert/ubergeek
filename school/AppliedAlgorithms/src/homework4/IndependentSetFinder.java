package homework4;
import java.util.ArrayList;
import java.util.List;

// Tardos Ch. 6, Pr. 1
class IndependentSetFinder {

	List<Integer> path; // The path that we want to find the max independent set of
	List<IndependentSet> maxIndSets; // The max independent subsets of subsets.
	
	// Initialization
	IndependentSetFinder(List<Integer> path){
		this.path = path;
		maxIndSets = new ArrayList<IndependentSet>();
		for(int i = 0; i < path.size(); i++){
			maxIndSets.add(null);
		}
		maxIndSets.set(0, new IndependentSet());
		maxIndSets.set(1, new IndependentSet(1));
	}

	// Algorithm
	IndependentSet findMaxWeightSet(int size){
		if(maxIndSets.get(size) != null){
			return maxIndSets.get(size);
		}
		// Guaranteed path.size() >= 2 at this point
		IndependentSet prev = findMaxWeightSet(size - 1);
		IndependentSet prev2 = findMaxWeightSet(size - 2);
		
		if(prev.weight() > prev2.weight() + path.get(size - 1)){
			maxIndSets.set(size, prev);
		} else {
			maxIndSets.set(size, new IndependentSet(prev2, path.get(size - 1)));
		}
		return maxIndSets.get(size);
	}

	// END ALGORITHM (Data type definitions and helper methods below)	
	@SuppressWarnings("serial")
	class IndependentSet extends ArrayList<Integer> {
		
			IndependentSet(){super();}

			IndependentSet(int newNode){ this.add(newNode); }
			
			IndependentSet(IndependentSet iSet, int newNode){
				this.addAll(iSet);
				this.add(newNode);
			}
			
			int weight(){
				int w = 0;
				for(int i : this){ w += path.get(i); }
				return w;
			}
	}

}
