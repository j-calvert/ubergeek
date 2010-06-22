package scanner;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class CLI  extends Frame implements Runnable {
	private static final long serialVersionUID = 1L;

	private static final int PNTS = 12;
	private static final int B_SIZE = 20;

	private Renderer renderer;

	private String[] lines = new String[B_SIZE];
	Font font = new Font("Helvetica", Font.CENTER_BASELINE, PNTS);
	FontRenderContext frc = new FontRenderContext(null, false, true);

	
	public static void main(String[] args) {
		CLI frame = new CLI();
		frame.setSize(600, 400);
		frame.setTitle("Steve the HeAID");

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Panel scopeCanvas = new Panel();
		frame.add(scopeCanvas, "Center");
		frame.renderer = new AwtRenderer(scopeCanvas);
		new Thread(frame).start();
		frame.setVisible(true);
	}
	

	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s;
		try {
			while ((s = in.readLine()) != null	) {
				for(int i = B_SIZE - 1; i-->1;) {
					lines[i] = lines[i - 1];
				}
				lines[0] = s;
				List<Shape> frame = new ArrayList<Shape>();
				for(int i = 0; i < B_SIZE; i++) {
					String l = lines[i];
					if(l != null) {						
						GlyphVector gv = font.createGlyphVector(frc, l);
						frame.add(gv.getOutline(PNTS, PNTS * (B_SIZE - i)));
					}
				}
				renderer.nextFrame(frame);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
