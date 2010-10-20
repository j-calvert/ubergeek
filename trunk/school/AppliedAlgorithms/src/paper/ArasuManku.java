// File: ArasuManku.java
package paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Deterministic time-based, sliding window approximation of the top occurring
 * items in a data stream.
 * 
 * Based on Section 5 of "Approximate Counts and Quantiles over Sliding Windows"
 * 
 * @param <Value>
 *            The class of values being ranked
 */
public class ArasuManku<Value> {

	private int L = 16; // The number of levels we use. We cast 2^L to an
						// integer, so need to respect that bound.
	private int M = 32; // The log of the capacity of the window we allow
	private long n = 0; // The data series ticker
	public long oneOverEpsilon = (long) Math.pow(2, L - 2);
	public long W = (long) Math.pow(2, M);
	private long period = 7 * 24 * 60 * 60 * 1000;

	List<List<Sketch>> activeSketches = new ArrayList<List<Sketch>>();
	List<Sketch> underConstruction = new ArrayList<Sketch>();

	/**
	 * @param L   The number of levels we use (minus 1).
	 * @param M   The log of the total number of samples that we can handle
	 *            within the period.
	 * @param period   The number of milliseconds in the period over which we're
	 *            tracking the topN.
	 * @param time The initial time that this stream is being monitored. In
	 *            non-test scenario, should be System.currentTimeMillis() in
	 *            first run, and persisted between consecutive runs
	 */
	public ArasuManku(int L, int M, long period, long time) {
		this.L = L;
		this.M = M;
		for (int l = 0; l <= L; l++) { // Initialize levels
			underConstruction.add(l, new Sketch(l, n, time));
			activeSketches.add(new ArrayList<Sketch>());
		}
	}

	public static void main(String[] args){
		ArasuManku au = new ArasuManku(16, 32, 0, 0);
		System.out.println(au.W + ": W");
		System.out.println(au.oneOverEpsilon + ": 1/e");
	}
	
	
	/**
	 * Insert a value in the data stream at time t
	 * 
	 * @param v The value of the data stream element
	 * @param time The physical time that this entrie is being added. Subsequent
	 *            calls must have non-decreasing values of time. In non-test
	 *            scenarios, should be System.currentTimeMillis()
	 */
	public void insert(Value v, long time) {
		n++;
		for (int l = 0; l <= L; l++) {
			Sketch s = underConstruction.get(l);
			if (s.insert(v, n)) { // If we graduated this sketch
				underConstruction.remove(l);
				underConstruction.add(l, new Sketch(l, n, time));
				activeSketches.get(l).add(s);
			}
		}
		expireOld(time);
	}

	private void expireOld(long time) {
		List<Sketch> level0 = activeSketches.get(0);
		if (level0.size() < 1) {
			return;
		}
		Sketch oldestLevel0 = level0.get(level0.size() - 1);
		if (time - oldestLevel0.creationTime > period) {
			long expiredIndex = oldestLevel0.streamIndex;
			for (int l = 0; l < activeSketches.size(); l++) {
				List<Sketch> level_l = activeSketches.get(l);
				if (level_l.size() > 0
						&& level_l.get(level_l.size() - 1).streamIndex <= expiredIndex) {
					level_l.remove(level_l.size() - 1);
				}
			}
		}
	}

	/**
	 * Returns a map of elements to approximate frequency counts, for all elements occuring over 1/N 
	 * @param time
	 */
	public Map<Value, Integer> getTopN(long time) {
		expireOld(time);
		Map<Value, Integer> topN = new HashMap<Value, Integer>();
		for (Sketch sketch : getTopActiveSketches()) {
			for (Entry<Value, Integer> entry : sketch.mg.counts.entrySet()) {
				int newCount = entry.getValue();
				if (topN.containsKey(entry.getKey())) {
					newCount += topN.get(entry.getKey());
				}
				topN.put(entry.getKey(), newCount);
			}
		}
		return topN;
	}

	private List<Sketch> getTopActiveSketches() {
		List<Sketch> topSketches = new ArrayList<Sketch>();
		List<Integer> activeIdxs = new ArrayList<Integer>();
		for (int i = 0; i <= L; i++) {
			activeIdxs.add(i, activeSketches.get(i).size() - 1);
		}
		boolean complete = false;
		int l = 0;
		while (!complete) {
			if (activeIdxs.get(l) >= 0) {
				if (l < L
						&& activeIdxs.get(l + 1) >= 0
						&& activeSketches.get(l).get(activeIdxs.get(l)).streamIndex <= activeSketches
								.get(l + 1).get(activeIdxs.get(l + 1)).streamIndex) {
					l++;
				} else {
					topSketches.add(activeSketches.get(l)
							.get(activeIdxs.get(l)));
					stepActiveIdxs(l, activeIdxs);
				}
			} else { l--; }
			if (l == 0 && activeIdxs.get(l) < 0) {
				complete = true;
			}
		}
		return topSketches;
	}

	private void stepActiveIdxs(int l, List<Integer> activeIdxs) {
		for (int i = l; i >= 0; i--) { // Step
			activeIdxs.set(i, (int) (activeIdxs.get(i) - Math.pow(2, l - i)));
		}
	}

	class LevelBlock {
		int l; // level
		long oneOverEpsilon_l; // 1 over error at this level
		long N_l; // size of block

		LevelBlock(int l) {
			oneOverEpsilon_l = (long) (2 * (2 + L) * Math.pow(2, l + 2)); // need
			N_l = (long) Math.pow(2, M - L - 2 - 2 + l);
		}

	}

	enum SketchState { UNDER_CONSTRUCTION, ACTIVE, EXPIRED; }

	class Sketch {
		SketchState state;
		long streamIndex; // The first index in the block
		LevelBlock levelBlock; // The size of the block
		MisraGries<Value> mg; // The top items
		long creationTime; // Used for time-based deletion, only really used for level-0 Sketches

		Sketch(int l, long streamIndex, long creationTime) {
			state = SketchState.UNDER_CONSTRUCTION;
			this.streamIndex = streamIndex;
			this.levelBlock = new LevelBlock(l);
			this.mg = new MisraGries<Value>(levelBlock.oneOverEpsilon_l);
			this.creationTime = creationTime;
		}

		// Inserts into underlying MisaGries, returns true if this Sketch Activates
		boolean insert(Value v, long n) {
			mg.insert(v);
			if (levelBlock.N_l <= n - streamIndex) {
				state = SketchState.ACTIVE;
				return true;
			} else {
				return false;
			}
		}
	}
}
