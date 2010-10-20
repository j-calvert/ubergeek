package homework5;

import java.util.ArrayList;

//Tardos Ch. 6, Pr. 19
public class SignalDeinterleaver {

boolean deinterleave(Signal s, Signal sigX, Signal sigY){
	for(int i = 0; i < s.size(); i++){
		if(deinterleavePart(s, sigX, sigY, i, s.size() - i)){ return true; }
	}
	return false;	
}
	
boolean deinterleavePart(Signal s, Signal sigX, Signal sigY, int numX, int numY){
	if(s.size() != numX + numY){ return false; }
	if(s.size() == 0){ return true; }
	if(s.get(s.size() - 1) == sigX.get((numX - 1) % sigX.size())){ 
		// The last element could have come from sigX
		return deinterleavePart(s.subList(0, s.size() - 1), sigX, sigY, numX - 1, numY);
	} else if (s.get(s.size() - 1) == sigY.get((numY - 1) % sigY.size())){ 
		// The last element could have come from sigY
		return deinterleavePart(s.subList(0, s.size() - 1), sigX, sigY, numX, numY - 1);
	}
	return false;
}

// END ALGORITHM (Data type definitions and helper methods below)	
static class Signal extends ArrayList<Boolean>{
	@Override
	public Signal subList(int fromIndex, int toIndex) {
		return (Signal) super.subList(fromIndex, toIndex);
	}
}
}
