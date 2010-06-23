package kitebot.bot;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


public class Dashboard extends Component {
    private static final long serialVersionUID = 1L;

    private int width, height, posX, posY, lastY = 0;
    
    private double lineForceFraction = 0;
    
	public Dashboard(int posX, int posY, int width, int height) {
        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
        enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
		setBounds(new Rectangle(posX, posY, width, height));
		setEnabled(true);
    }
    
    public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(posX, posY, width, lastY);
		g.setColor(Color.blue);
		g.fillRect(posX, posY + lastY, width, height - lastY);
    }

	public void processEvent(AWTEvent e) {
		lastY = ((MouseEvent) e).getY();
		lastY = lastY < 0 ? 0 : lastY;
		lastY = lastY > height ? height : lastY;
		lineForceFraction = 1d * (height - lastY) / height;
//    	System.out.println(lineForceFraction);
    }

	public double getLineForceFraction() {
		return lineForceFraction;
	}
}
