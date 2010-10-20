package homework4;
import java.util.ArrayList;
import java.util.List;

//Tardos Ch. 5, Pr. 5
public class VisibleLineEvaluator {

	List<VisibleLine> visibleLines(List<Line> lines) {
		List<VisibleLine> ret = new ArrayList<VisibleLine>();
		if (lines.size() == 0) { return ret; }
		if (lines.size() == 1) { ret.add(new VisibleLine(lines.get(0), Float.MAX_VALUE)); }
		int halfSize = lines.size() / 2;
		return merge(visibleLines(lines.subList(0, halfSize)),
				visibleLines(lines.subList(halfSize, lines.size())));
	}

	List<VisibleLine> merge(List<VisibleLine> linesA, List<VisibleLine> linesB) {
		List<VisibleLine> ret = new ArrayList<VisibleLine>();
		int nextA = 0, nextB = 0;
		float lastX = -Float.MAX_VALUE;
		while (nextA < linesA.size() || nextB < linesB.size()) {
			VisibleLine lineA = linesA.get(nextA);
			VisibleLine lineB = linesB.get(nextB);
			boolean nextXisA = lineA.rightBound < lineB.rightBound;
			float nextX = nextXisA ? lineA.rightBound : lineB.rightBound;
			boolean winnerA = lineA.height(nextX) > lineB.height(nextX);
			if (winnerA && (lineA.height(lastX) < lineB.height(lastX))) {
				float intersectX = lineA.intersect(lineB);
				ret.add(new VisibleLine(winnerA ? lineA : lineB, intersectX));
				lastX = intersectX;
			} else {
				if (nextXisA) {
					nextA++;
					lastX = lineA.rightBound;
					if (winnerA) { ret.add(new VisibleLine(lineA)); }
				} else {
					nextB++;
					lastX = lineB.rightBound;
					if (!winnerA) { ret.add(new VisibleLine(lineB)); }
				}
			}
		}
		return ret;
	}

	// END ALGORITHM (Data type definitions and helper methods below)	
	static class Line {
		float m;
		float b;

		Line(float m, float b) {
			this.m = m;
			this.b = b;
		}

		float height(float x) {
			if (x == -Float.MAX_VALUE) { return (m == 0 ? b : -m); }
			if (x == Float.MAX_VALUE) { return (m == 0 ? b : m); }
			return m * x + b;
		}
		
		float intersect(Line l){
			return (this.b - l.b) / (l.m - this.m);
		}

	}

	public static class VisibleLine extends Line {
		float rightBound;

		public VisibleLine(Line l, float rightBound) {
			super(l.m, l.b);
			this.rightBound = rightBound;
		}

		public VisibleLine(VisibleLine l) {
			super(l.m, l.b);
			this.rightBound = l.rightBound;
		}
	}
}
