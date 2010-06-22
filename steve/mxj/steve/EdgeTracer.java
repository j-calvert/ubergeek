package steve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgeTracer extends Tracer {

	protected void getTraces(int w, int h, int[] data) {
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
					readTraces.add(edge);
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
}
