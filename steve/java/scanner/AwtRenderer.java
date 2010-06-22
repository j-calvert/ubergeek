package scanner;

import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Shape;
import java.util.Collection;


public class AwtRenderer implements Renderer {

	Collection<Shape> currentFrame;
	Panel panel;

	public AwtRenderer(Panel panel) {
		this.panel = panel;
	}

	public void nextFrame(Collection<Shape> frame) {
		currentFrame = frame;
		if (panel.isVisible()) {
			Graphics2D g = (Graphics2D) panel.getGraphics();
			g.clearRect(0, 0, 400, 400);
			if (g != null) {
				for (Shape s : currentFrame) {
					// g.setXORMode(panel.getBackground());
					g.draw(s);
				}
			}
		}
	}

}