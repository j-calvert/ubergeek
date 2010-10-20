package edu.washington.csep576;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.PrimaryIndex;

public class ImageSearch {

	private static final String dbDir = "data";
	private static final String tmpDbDir = "tmpData";
	
	private static Store tmpStore;
	
	public static Store store;
	
	public static List<Regions> regions = new ArrayList<Regions>();
	
	public static void main(String[] args) throws Exception {
		System.out.println("Starting up with arg: " + (args.length > 0 ? args[0] : "NULL"));
		store = new Store();
		store.setup(new File(dbDir), false);
		tmpStore = new Store();
		tmpStore.setup(new File(tmpDbDir), false);
		if(args.length > 0 && args[0].equalsIgnoreCase("setup")){
			List<String> keys = new ArrayList<String>();
			for(File f : Util.find("images", ".ppm")){
				String key = Util.keyFromFilename(f.getName());
				keys.add(key);
				System.out.println(f);
				PPM ppm = new PPM(f);
				ConCompClusters ccClusters = tmpStore.getEntityStore().getPrimaryIndex(String.class, ConCompClusters.class).get(key);
				if(ccClusters != null){
					System.out.println("Found, skipping");
				} else {
					Kmeans kmeans = new Kmeans(key, ppm, 3.1d, 16);
					ccClusters = kmeans.makeCCClusters();
					tmpStore.getEntityStore().getPrimaryIndex(String.class, ConCompClusters.class).put(ccClusters);
				}
				Regions regions = ccClusters.getRegions(ppm);
				store.getEntityStore().getPrimaryIndex(String.class, Regions.class).put(regions);
			}
			populateCooccur(keys);
		}
		slurpInRegions();
		new UI();
		System.out.println("Please point a browser to http://localhost:8765/");
	}
	
	private static void populateCooccur(List<String> keys) throws DatabaseException {
		Collections.sort(keys);
		for(int i = 0; i < keys.size(); i++){
			String key1 = keys.get(i);
			ConCompClusters ccc1 = tmpStore.getEntityStore().getPrimaryIndex(String.class, ConCompClusters.class).get(key1);
			for(int j = i + 1; j < keys.size(); j++){
				String key2 = keys.get(j);
				ConCompClusters ccc2 = tmpStore.getEntityStore().getPrimaryIndex(String.class, ConCompClusters.class).get(key2);
				Cooccurence.save(key1, key2, ccc1.numComps, ccc2.numComps, ccc1.compAlloc, ccc2.compAlloc);
			}
		}
	}

	private static void slurpInRegions() throws Exception {
		PrimaryIndex<String, Regions> pKey = store.getEntityStore().getPrimaryIndex(String.class, Regions.class);
		for(String key : pKey.keys(null, null)){
			regions.add(pKey.get(key));
		}
	}
	
	public static void setScores(String query, Sort sort) throws DatabaseException {
		Regions q = null;
		for(Regions r : regions){
			if(r.name.equals(query)){
				q = r;
				break;
			}
		}
		for(Regions r : regions){
			r.setScore(q, sort);
		}		
	}
}
