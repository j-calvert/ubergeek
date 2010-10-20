package edu.washington.csep576;

import java.io.File;
import java.util.Comparator;
import java.util.TreeSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Regions {

	@PrimaryKey
	String name;
	
	Region[] regions;

	transient double score;
	
	public Regions(){}
	
	public Regions(String name, int numComps, int[][] compAlloc, double[][] clusters, int[][] clusterAlloc, PPM ppm){
		this.name = name;
		this.regions = new Region[numComps];
		for(int x = 0; x < compAlloc.length; x++){
			for(int y = 0; y < compAlloc[0].length; y++){
				if(compAlloc[x][y] == 0){
					continue;
				}
				int i = compAlloc[x][y] - 1;
				if(regions[i] == null){
					regions[i] = new Region(clusters[clusterAlloc[x][y]]);
				}
				Region region = regions[i];
				if(!region.area.containsKey(x)){
					region.area.put(x, new TreeSet<Integer>());
				}
				region.area.get(x).add(y);
			}
		}
		for(Region r : regions){
			r.computeTexture(ppm);
			r.computeGeometry();
		}
	}
	
	public static File getThumb(String name){
		String[] parts = name.split("\\.");
		String[] subs = parts[0].split("_");
		return new File("/" + subs[0] + "/" + parts[0] + "t.jpg");	
	}
	
	public void setScore(Regions q, Sort sort) throws DatabaseException {
		if(q.name.equals(this.name)){ this.score = 0; return; }
		int[][] co = Cooccurence.get(q.name, this.name);
		if(sort == Sort.COLOR){
			setColorDistance(co, q);
		} else if(sort == Sort.TEXTURE){
			setTextureDistance(co, q);
		} else {
			setColorAndTextureDistance(co, q);
		}
		
	}
	
	private void setColorAndTextureDistance(int[][] co, Regions q) {
		setColorDistance(co, q);
		double cScore = score;
		setTextureDistance(co, q);
		score = Math.sqrt(cScore * cScore + score * score);
	}

	private void setTextureDistance(int[][] co, Regions q) {
		long total = 0;
		score = 0;
		for(int i = 0; i < co.length; i++){
			for(int j = 0; j < co[i].length; j++){
				total += co[i][j];
				score += co[i][j] 
				               	* Util.distance(
				               			q.regions[i].blpDist,
				               			regions[j].blpDist);
			}
		}
		score = score / total;
	}

	private void setColorDistance(int[][] co, Regions q){
		long total = 0;
		score = 0;
		for(int i = 0; i < co.length; i++){
			for(int j = 0; j < co[i].length; j++){
				total += co[i][j];
				score += co[i][j] 
				               	* Util.distance(
				               			q.regions[i].color,
				               			regions[j].color);
			}
		}
		score = score / total;
	}
	
	static class RegionsComparator implements Comparator<Regions> {

		public int compare(Regions o1, Regions o2) {
			if(o1.score > o2.score) return 1;
			else if(o1.score < o2.score) return -1;
			else return 0;
		}
		
	}


	
}
