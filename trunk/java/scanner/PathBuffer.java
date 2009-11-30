package scanner;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PathBuffer extends Thread {

	private static class ScannerPoint {
		private static final int MOVE = PathIterator.SEG_MOVETO;
		private static final int DRAW = PathIterator.SEG_LINETO;
		private static int MAX = (int) Math.pow(2, 16) - 1;
		byte[] x = new byte[2];
		byte[] y = new byte[2];
		int mode;

		ScannerPoint(float x, float y, int mode) {
			System.out.println("Creating point (" + x + ", " + y + ")");
			this.x = floatToTwoByte(x);
			this.y = floatToTwoByte(y);
			this.mode = mode;
		}

		private static byte[] floatToTwoByte(float x) {
			byte[] ret = new byte[2];
			int M = 120;
			x = x + 10;
			if (x > M) {
				x = M;
			}
			if (x < 0) {
				x = 0;
			}
			int xi = Math.round(MAX / M * x);
			ret[0] = (byte) (xi & 255);
			ret[1] = (byte) (xi >> 8);
			return ret;
		}

		@Override
		public String toString() {
			return "([" + x[0] +"," + x[1] + "], [" + y[0] + "," + y[1] + "])";
		}
		
		
	}

	// private AffineTransform affineTransform = new AffineTransform();
	private List<ScannerPoint> scannerPoints = new ArrayList<ScannerPoint>();
	private boolean run = true;

	public PathBuffer() throws IOException {
		try {
			sdl = AudioSystem.getSourceDataLine(af);
			sdl.open(af);
		} catch (LineUnavailableException e) {
			throw new IOException(e);
		}
	}

	public static void main(String[] args) throws IOException {
		Font font = new Font("Arial", Font.PLAIN, 12);
		FontRenderContext frc = new FontRenderContext(null, true, true);
		PathBuffer tgt = new PathBuffer();
		tgt.start();

		GlyphVector gv = font.createGlyphVector(frc, "Hello World!");
		Shape glyph = gv.getOutline(0, 0);
		tgt.generatePathTone(glyph);

		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(System.in));
		// String s;
		// while ((s = in.readLine()) != null && s.length() != 0) {
		// GlyphVector gv = font.createGlyphVector(frc, s);
		// Shape glyph = gv.getOutline(0, 0);
		// tgt.generatePathTone(glyph);
		// }
	}

	public void generatePathTone(Shape path) {
		PathIterator pi = path.getPathIterator(null);
		float[] seg = new float[6];
		float x = 0, y = 0, mx = 0, my = 0;
		List<ScannerPoint> points = new ArrayList<ScannerPoint>();
		while (!pi.isDone()) {
			int segtype = pi.currentSegment(seg);
			int mode = 0;
			switch (segtype) {
			case PathIterator.SEG_MOVETO:
				x = mx = seg[0];
				y = my = seg[1];
				mode = ScannerPoint.MOVE;
				break;
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_QUADTO:
				// TODO Interpolate Quadratics
			case PathIterator.SEG_CUBICTO:
				// TODO Interpolate Cubics
				x = seg[0];
				y = seg[1];
				mode = ScannerPoint.DRAW;
				break;
			case PathIterator.SEG_CLOSE:
				x = mx;
				y = my;
				mode = ScannerPoint.DRAW;
				break;
			} // switch
			ScannerPoint point = new ScannerPoint(x, y, mode);
			System.out.println("Adding point " + point);
			points.add(point);
			pi.next();
		}
		scannerPoints = points;
	}

	private static final AudioFormat af = new AudioFormat(48000, 16, 6, true,
			false);
	private static final int X_CHAN = 2;
	private static final int Y_CHAN = 3;
	private static final int B_CHAN = 5;
	private static final byte[][] B_SIG = new byte[][] {
			ScannerPoint.floatToTwoByte(0f), ScannerPoint.floatToTwoByte(1f) };
	private SourceDataLine sdl;

	@Override
	public void run() {

		int m = 3;
		byte[] buf = new byte[12];

		sdl.start();
		while (run) {
			for (ScannerPoint point : scannerPoints) {
				for (int k = 0; k < m; k++) {
					for (int i = 0; i < 6; i++) {
						switch (i) {
						case X_CHAN:
							buf[2 * i] = point.x[0];
							buf[2 * i + 1] = point.x[1];
							break;
						case Y_CHAN:
							buf[2 * i] = point.y[0];
							buf[2 * i + 1] = point.y[1];
							break;
						case B_CHAN:
							if (point.mode == ScannerPoint.MOVE) {
								buf[2 * i] = B_SIG[0][0];
								buf[2 * i + 1] = B_SIG[0][1];
							} else {
								buf[2 * i] = B_SIG[1][0];
								buf[2 * i + 1] = B_SIG[1][1];
							}
							break;
						default:
							buf[2 * i] = 0;
							buf[2 * i + 1] = 0;
						}

					}
					sdl.write(buf, 0, 12);
				}
			}
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}

}
