package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class TppGenerator {
    int numCities;
    int numProducts;
    int maxTime;
    int prodPerCity;

    Random rand = new Random();

    private static String nl = System.getProperty("line.separator");

    TppGenerator(int numCities, int numProducts, int maxTime,
            int maxPortionProdPerCity) {
        this.numCities = numCities;
        this.numProducts = numProducts;
        this.maxTime = maxTime;
        this.prodPerCity = maxPortionProdPerCity;

    }

    public void generateRules() throws Exception {
        List<At> ats = allocateProducts();

        FileWriter fstream = new FileWriter("c" + numCities + "p" + numProducts
                + "t" + maxTime + "a" + prodPerCity + ".db");
        BufferedWriter out = new BufferedWriter(fstream);

        out.write(successorPredicates());
        out.write(inPredicates());
        out.write(visitedPredicates());
        out.write(atPredicates(ats));
        out.write(havePredicates(false, 0));
        out.write("//Goal State" + nl);
        out.write("In(City0, T" + maxTime + ")" + nl);
        out.write(havePredicates(true, maxTime));
        out.close();
    }

    private List<At> allocateProducts() {
        List<At> ats = new ArrayList<At>();
        for (int i = 0; i < numCities; i++) {
            ats.addAll(allocateProductsForOneCity(i));
        }
        return ats;
    }

    private List<At> allocateProductsForOneCity(int city) {
        List<At> ats = new ArrayList<At>();
        Set<Integer> added = new HashSet<Integer>(); // so we don't have dups
        for (int i = 0; i < prodPerCity; i++) {
            int prod = rand.nextInt(numProducts);
        	if(added.contains(prod)){
        		continue; // Will have fewer products than avg.
        	}
            ats.add(new At(city, prod));
            added.add(prod);
        }
        return ats;
    }

    private String successorPredicates() {
        String ret = "// Successor predicates" + nl;
        for (int t = 0; t < maxTime; t++) {
            ret += "SuccTime(T" + t + ", T" + (t + 1) + ")" + nl;
        }
        ret += nl;
        return ret;
    }

    private String inPredicates() {
        String ret = "// Initial state" + nl;
        ret += "In(City0, T0)" + nl;
        for (int c = 1; c < numCities; c++) {
            ret += "!In(City" + c + ", T0)" + nl;
        }
        return ret;
    }

    private String visitedPredicates() {
        String ret = "";
        for (int c = 0; c < numCities; c++) {
            ret += "!Visited(City" + c + ", T0)" + nl;
        }
        return ret;
    }

    private String havePredicates(boolean have, int t) {
        String ret = "";
        for (int p = 0; p < numProducts; p++) {
            ret += (have ? "" : "!") + "Have(Prod" + p + ", T" + t + ")" + nl;
        }
        ret += nl;
        return ret;
    }

    private String atPredicates(List<At> ats) {
        String ret = "";
        for(At at : ats){
        	ret += at.toString();
        }
        return ret;
    }

    private static class At {
        int city;
        int prod;

        At(int city, int prod) {
            this.city = city;
            this.prod = prod;
        }

        public String toString() {
            return "At(City" + city + ",Prod" + prod + ")" + nl;
        }

		@Override
		public boolean equals(Object arg0) {
			if(! (arg0 instanceof At)){
				return false;
			} else {
				return ((At) arg0).city == this.city && ((At) arg0).prod == this.prod;
			}
		}
    }
    
    public static void main(String[] args) throws Exception {
        for(int cities = 12 ; cities <= 28; cities += 4){
            for(int products = cities; products <= cities + 2; products++){
                for(int time = cities / 2; time <= cities; time += 3){
                    for(int ppc = 5; ppc <= 10; ppc += 5){
                        TppGenerator tg = new TppGenerator(cities, products, time, ppc);
                        tg.generateRules();
                    }
                }
            }
        }
    }
}
