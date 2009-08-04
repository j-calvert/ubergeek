package steve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.cycling74.jitter.JitterMatrix;
import com.cycling74.max.DataTypes;
import com.cycling74.msp.MSPPerformer;
import com.cycling74.msp.MSPSignal;

public class EdgeTracer extends MSPPerformer {
	JitterMatrix jm = new JitterMatrix();

	private static final int[][] nbrs = new int[][] { { 1, 0 }, { 1, -1 },
			{ 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

	List<List<Coord>> readEdges = new ArrayList<List<Coord>>();
	List<List<Coord>> writeEdges = new ArrayList<List<Coord>>();

	EdgeTracer() {
		declareInlets(new int[] { DataTypes.ALL });
		declareOutlets(new int[] { SIGNAL, SIGNAL, SIGNAL });

		setInletAssist(INLET_ASSIST);
		setOutletAssist(OUTLET_ASSIST);
		declareAttribute("spp");
	}

	float scale;

	private int edgeIndex;
	private int pointIndex;
	private int spp = 10;

	private int stallIndex;

	public void jit_matrix(String s) {
		jm.frommatrix(s);
		int dim[] = jm.getDim();
		int[] data = new int[dim[0] * dim[1]];
		scale = Math.max(dim[0], dim[1]);
		jm.copyMatrixToArray(data);
		traceEdges(dim[0], dim[1], data);
		advanceFrame();
	}

	private void advanceFrame() {
		post("advancing frame with this many edges: " + readEdges.size());
		writeEdges = readEdges;
		resetIndexes();
		readEdges = new ArrayList<List<Coord>>();
	}

	private void resetIndexes() {
		this.edgeIndex = 0;
		this.pointIndex = 0;
		this.stallIndex = 0;
	}

	private void traceEdges(int w, int h, int[] data) {
		Set<Coord> traced = new HashSet<Coord>();
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				Coord c = new Coord(i, j);
				if (getPixel(data, w, i, j) > 0 && !traced.contains(c)) {
					List<Coord> edge = new ArrayList<Coord>();
					while (c != null) {
						edge.add(c);
						traced.add(c);
						c = trace(traced, c, edge, w, h, data);
					}
					// Trace backwards
					Collections.reverse(edge);
					c = edge.remove(edge.size() - 1);
					while (c != null) {
						edge.add(c);
						traced.add(c);
						c = trace(traced, c, edge, w, h, data);
					}
					// Re-reverse
					Collections.reverse(edge);
					readEdges.add(edge);
				}
			}
		}
	}

	private Coord trace(Set<Coord> traced, Coord c, List<Coord> edge, int w,
			int h, int[] data) {
		for (int i = 0; i < nbrs.length; i++) {
			int xn = c.x + nbrs[i][0];
			int yn = c.y + nbrs[i][1];
			if (xn < 0 || yn < 0 || xn >= w || yn >= h) {
				continue;
			}
			if (getPixel(data, w, xn, yn) > 0) {
				Coord cn = new Coord(xn, yn);
				if (!traced.contains(cn)) {
					return cn;
				}
			}
		}
		return null;
	}

	private int getPixel(int[] data, int w, int i, int j) {
		return data[i + j * w];
	}

	private class Coord {
		int x, y;

		Coord(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Coord) {
				if (((Coord) obj).x == x && ((Coord) obj).y == y) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return x + y * 640;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}

	}

	// / MAX Stuff
	private static final String[] INLET_ASSIST = new String[] { "messages in" };

	private static final String[] OUTLET_ASSIST = new String[] { "x out",
			"y out", "blanking out" };

	
	
	@Override
	public void perform(MSPSignal[] in, MSPSignal[] out) {
		for (int i = 0; i < out[0].vec.length; i++) {
			try {
				if (stallIndex >= spp) {
					stallIndex = 0;
					pointIndex++;
				} else {
					stallIndex++;
				}
				if (pointIndex >= this.writeEdges.get(edgeIndex).size()) {
					edgeIndex++;
					pointIndex = 0;
					stallIndex = 0;
				}
				if (edgeIndex >= this.writeEdges.size()) {
					edgeIndex = 0;
					pointIndex = 0;
					stallIndex = 0;
				}
				out[0].vec[i] = (float) (this.writeEdges.get(edgeIndex).get(pointIndex).x * (1.0 / scale));
				out[1].vec[i] = (float) (this.writeEdges.get(edgeIndex).get(pointIndex).y * (1.0 / scale));
				out[2].vec[i] = (float) (blank(pointIndex));
				post(edgeIndex + ": " + pointIndex + ": " + stallIndex);
			} catch (Exception e) {
				post("exception:" + e.getMessage());
				post(edgeIndex + ": " + pointIndex + ": " + stallIndex);
				resetIndexes();
				// Prolly an NPE or OOBE caused by a race condition

			}
		}
	}

	private static final int LAS_OFF = 1;
	private static final int LAS_ON = 0;
	private float blank(int pointIndex) {
		return pointIndex == 0 ? LAS_OFF : LAS_ON;
	}

	public static void main(String[] args) {
		long total = 0;
		for (int j = 0; j < 30; j++) {
			int[] data = new int[240 * 320];
			Random r = new Random();
			for (int i = 0; i < data.length; i++) {
				data[i] = r.nextFloat() > .9 ? 1 : 0;
			}
			long start = System.currentTimeMillis();
			EdgeTracer efb = new EdgeTracer();
			efb.traceEdges(240, 320, data);
			total += System.currentTimeMillis() - start;
			// for (List<Coord> edge : efb.edges) {
			// for (Coord c : edge) {
			// System.out.print(c + " ");
			// }
			// System.out.println();
			// }
		}
		System.out.println("Total time in ms: " + total);
	}

}
