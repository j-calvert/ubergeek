package edu.washington.csep576;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
@Entity
public class ConCompClusters {

	@PrimaryKey
	String name;
	// clusterId --> Color
	double[][] clusters;
	// (x, y) --> clusterId
	int[][] clusterAlloc;
	// (x, y) --> componentId
	int[][] compAlloc;
	// number components (post filtering tiny components).
	int numComps;
	
	public ConCompClusters(){}

	public ConCompClusters(String name, double[][] clusters, int[][] clusterAlloc) {
		this.name = name;
		this.clusters = clusters;
		this.clusterAlloc = clusterAlloc;
		this.compAlloc = new int[clusterAlloc.length][clusterAlloc[0].length];
		allocComp();
	}

	private void allocComp() {
		Map<Integer, Set<Coord>> compAllocMap = new HashMap<Integer, Set<Coord>>();
		int compId = 1;
		for (int x = 0; x < clusterAlloc.length; x++) {
			for (int y = 0; y < clusterAlloc[0].length; y++) {
				if (x > 0) {
					if (clusterAlloc[x - 1][y] == clusterAlloc[x][y]) {
						if (compAlloc[x][y] == 0) {
							compAlloc[x][y] = compAlloc[x - 1][y];
							compAllocMap.get(compAlloc[x][y]).add(new Coord(x,y));
						} else {
							combineComps(compAllocMap, compAlloc,
									compAlloc[x - 1][y], compAlloc[x][y]);
						}
					}
				}
				if (y > 0) {
					if (clusterAlloc[x][y - 1] == clusterAlloc[x][y]) {
						if (compAlloc[x][y] == 0) {
							compAlloc[x][y] = compAlloc[x][y - 1];
							compAllocMap.get(compAlloc[x][y]).add(new Coord(x,y));
						} else {
							combineComps(compAllocMap, compAlloc,
									compAlloc[x][y - 1], compAlloc[x][y]);
						}
					}
				}
				// if compAlloc hasn't been set by this point, then it won't by
				// an adjacent cluster.
				if(compAlloc[x][y] == 0){
					compId++;
					compAlloc[x][y] = compId;
					compAllocMap.put(compId, new HashSet<Coord>());
					compAllocMap.get(compId).add(new Coord(x, y));
				}
			}
		}
		filterComponents(compAllocMap);
	}

	private static void combineComps(Map<Integer, Set<Coord>> compAllocMap,
			int[][] compAlloc, int allocId1, int allocId2) {
		if(allocId1 == allocId2){ return; }
		for (Coord c : compAllocMap.get(allocId2)) {
			compAlloc[c.x][c.y] = allocId1;
			compAllocMap.get(allocId1).add(c);
		}
		compAllocMap.remove(allocId2);
	}
	
	private void filterComponents(Map<Integer, Set<Coord>> compAllocMap){
		int i = 0;
		for(int compId : compAllocMap.keySet()){
			boolean in = compAllocMap.get(compId).size() > 500; 
			if(in){ i++; }
			for(Coord c : compAllocMap.get(compId)){
				compAlloc[c.x][c.y] = in ? i : 0;
			}
		}
		numComps = i;
	}
	
	public Regions getRegions(PPM ppm){
		return new Regions(name, numComps, compAlloc, clusters, clusterAlloc, ppm);
	}
	
	static class Coord {
		int x;
		int y;
		Coord(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
}
