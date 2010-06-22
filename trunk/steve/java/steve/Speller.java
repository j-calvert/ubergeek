package steve;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.msp.MSPPerformer;
import com.cycling74.msp.MSPSignal;

public class Speller extends MSPPerformer {

	private int fontSize = 12;
	private String text = null;
	private String readText = null;
	private Shape textOutline = null;
	private PathIterator pi;
	private FontRenderContext frc = new FontRenderContext(null, false, true);
	private float miny;
	private float maxy;
	private int pnts;
	private String fontName = "Helvetica";
	private int stall = 5;

	private static final String[] INLET_ASSIST = new String[] { "text",
			"fontSize" };

	private static final String[] OUTLET_ASSIST = new String[] { "x out",
			"y out", "blanking out" };

	protected Speller() {
		declareInlets(new int[] { DataTypes.ALL });
		declareOutlets(new int[] { SIGNAL, SIGNAL, SIGNAL });

		setInletAssist(INLET_ASSIST);
		setOutletAssist(OUTLET_ASSIST);
		declareAttribute("text");
		declareAttribute("fontSize");
		declareAttribute("fontName");
		declareAttribute("stall");
	}

	private void read() {
		if (readText != text && text != null) {
			readText = text;
			Font font = new Font(this.fontName, Font.CENTER_BASELINE, fontSize);
			GlyphVector gv = font.createGlyphVector(frc, readText);
			textOutline = gv.getOutline(fontSize, fontSize);
			pi = textOutline.getPathIterator(null);
			miny = 0;
			maxy = 4 * fontSize;
		}
	}

	float mx, my;

	@Override
	public void perform(MSPSignal[] in, MSPSignal[] out) {
		read();
		int i = 0;
		float[] seg = new float[6];
		int stallIdx = 0;
		if (pi == null) {
			return;
		}
		while (i < out[0].vec.length) {
			if (pi.isDone()) {
				pi = textOutline.getPathIterator(null);
				int info_idx = getInfoIdx();
				outlet(info_idx, new Atom[] { Atom.newAtom("glyph_points"),
						Atom.newAtom(pnts) });
				pnts = 0;
				pi.next();
				stallIdx = 0;
			} else if (stallIdx >= stall) {
				stallIdx = 0;
				pi.next();
				pnts++;
			} else {
				stallIdx++;
			}
			int segtype = pi.currentSegment(seg);
			boolean next = false;
			switch (segtype) {
			case PathIterator.SEG_MOVETO:
				out[0].vec[i] = mx = seg[0];
				out[1].vec[i] = mx = seg[1];
				out[2].vec[i] = 0;
				next = true;
				break;
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_QUADTO:
				// TODO Interpolate Quadratics
			case PathIterator.SEG_CUBICTO:
				// TODO Interpolate Cubics
				out[0].vec[i] = seg[0];
				out[1].vec[i] = seg[1];
				out[2].vec[i] = 1;
				next = true;
				break;
			case PathIterator.SEG_CLOSE:
				// out[0].vec[i] = mx;
				// out[1].vec[i] = my;
				// out[2].vec[i] = 1;
				break;
			} // switch
			if (out[1].vec[i] > maxy) {
				maxy = out[1].vec[i];
			}
			if (out[1].vec[i] < miny) {
				miny = out[1].vec[i];
			}
			float dy = maxy - miny;
			out[0].vec[i] = (out[0].vec[i] - miny) * 2 / dy - 1;
			out[1].vec[i] = (out[1].vec[i] - miny) * 2 / dy - 1;
			if (next) {
				i++;
			}
		}
	}

	public static void main(String[] args) {
		Speller s = new Speller();
		s.text = "dog";
		double d = 400;
		short sh = 1;
		MSPSignal[] out = new MSPSignal[] {
				new MSPSignal(new float[100], d, 1, sh),
				new MSPSignal(new float[100], d, 1, sh),
				new MSPSignal(new float[100], d, 1, sh),
				new MSPSignal(new float[100], d, 1, sh) };
		s.perform(null, out);

	}
}
