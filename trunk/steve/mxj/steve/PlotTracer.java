package steve;

import java.util.ArrayList;
import java.util.List;

public class PlotTracer extends Tracer {

	@Override
	protected void getTraces(int w, int h, int[] data) {
		if(w != 3) {
			throw new RuntimeException("Width of matrix sent to PlotsTracer must be 3");
		}
		this.scale = Math.max(data[1], data[2]);
		List<Coord> trace = new ArrayList<Coord>();
		for(int i = 1; i < h; i++) {
			if(data[i * 3] == 0) {
				if(trace.size() > 0) {
					readTraces.add(trace);
				}
				trace = new ArrayList<Coord>();
			} else {
				if(trace.size() > 0) {
					pixelByPixel(trace, new Coord(data[i * 3 + 1], data[i * 3 + 2]));
				} else {
					new Coord(data[i * 3 + 1], data[i * 3 + 2]);
				}
			}
		}
	}

	private void pixelByPixel(List<Coord> trace, Coord c1) {
		Coord c0 = trace.get(trace.size() - 1) ;
		int dist = c0.intDist(c1);
		for(int i = 1; i <= dist; i ++){
			int x = ((dist - i) * c0.x + dist + c1.x) / dist;
			int y = ((dist - i) * c0.y + dist + c1.y) / dist;
			trace.add(new Coord(x, y));
		}	
	}
}
