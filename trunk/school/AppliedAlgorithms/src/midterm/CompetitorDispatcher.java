package midterm;

public class CompetitorDispatcher {

public int compare(Competitor i, Competitor j){
//	 Order reversed to provide descending sort
	int T = 0;
	int k = T + i.s + i.b + i.r;
	return j.b + j.r - (i.b  + i.r); 
}
	
public static class Competitor {
	// swim; bike; run	
	int s; int b; int r;
}

}
