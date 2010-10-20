package edu.washington.csep576;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Computes and caches Regions overlaps
 */
@Entity
public class Cooccurence {

	@PrimaryKey
	String key;

	int[][] coa;
	
	public Cooccurence(){}
	
	public static void save(String key1, String key2, int num1, int num2, int[][] ca1, int[][] ca2) throws DatabaseException {
		Cooccurence co = new Cooccurence();
		co.key = key1 + key2;
		co.coa = new int[num1][num2];
		for(int x = 0; x < Math.min(ca1.length, ca2.length); x++){
			for(int y = 0; y < Math.min(ca1[0].length, ca2[0].length); y++){
				if(ca1[x][y] > 0 && ca2[x][y] > 0){
					co.coa[ca1[x][y] - 1][ca2[x][y] - 1]++;
				}
			}
		}
		ImageSearch.store.getEntityStore().getPrimaryIndex(String.class, Cooccurence.class).put(co);
	}

	public static int[][] get(String key1, String key2) throws DatabaseException {
		boolean transpose = key1.compareTo(key2) > 0;
		Cooccurence co = ImageSearch.store.getEntityStore().getPrimaryIndex(String.class, Cooccurence.class).get(transpose ? key2 + key1 : key1 + key2);
		return transpose ? co.transpose() : co.coa;
	}

	private int[][] transpose(){
		int[][] tc = new int[coa[0].length][coa.length];
		for(int i = 0; i < coa.length; i++){
			for(int j = 0; j < coa[i].length; j++){
				tc[j][i] = coa[i][j];
			}
		}
		return tc;
	}
	
}
