package steve;

import java.util.ArrayList;
import java.util.List;

public class PlotTracer2 extends Tracer2 {

	@Override
	protected void getTraces(int[] data) {
		int w = 3;
		List<Coord> trace = new ArrayList<Coord>();
		for(int i = 1; i < data.length / w; i++) {
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
