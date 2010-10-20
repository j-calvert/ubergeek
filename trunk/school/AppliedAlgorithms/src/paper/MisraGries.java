// File: MisraGries.java
package paper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MisraGries<Value> {
	
	public Map<Value, Integer> counts = new HashMap<Value, Integer>();
	
	public long k; // == 1/epsilon
	
	public MisraGries(long k){
		System.out.println(k);
		this.k = k;
	}
	
	public void insert(Value x){
		if(counts.containsKey(x)){
			counts.put(x, counts.get(x) + 1);
		} else if (counts.size() < k) {
			counts.put(x, 1);
		} else {
			Iterator<Value> it = counts.keySet().iterator();
			while(it.hasNext()){
				Value v = it.next();
				if(counts.get(v) == 1){
					it.remove();
				} else {
					counts.put(v, counts.get(v) - 1);
				}
			}
		}
	}
}
