package org.kitebot.gear;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class Gear extends Component {
    protected int gearCount = 50;
	protected int x = 0, y = 0, rad = 100;
    protected double angle = 0.0;
    protected Color color1 = Color.white;
    protected Color color2 = Color.blue;
    protected Color interior = Color.lightGray;
    protected int pixelGearDepth = 4;

    private double gearToothAngle;
    private double rSizeH;
    private double rSizeL;
    
    public Gear(int x, int y, int rad, int gearCount, int pixelGearDepth) {
    	this.x = x;
    	this.y = y;
    	this.rad = rad;
        this.gearCount = gearCount;
        this.pixelGearDepth = pixelGearDepth;

        gearToothAngle = 2 * Math.PI / gearCount;
        rSizeH = rad + (double) pixelGearDepth / 2;
        rSizeL = rad - (double) pixelGearDepth / 2;
}
    

    public void setAngle(double newAngle) {
        angle = newAngle % (2 * Math.PI);
        while (angle < 0) angle += 2 * Math.PI;
    }
    
    public int rotateX(double rad, double angle) {
        return (int) (x + rad * Math.cos(this.angle + angle));
    }

    public int rotateY(double rad, double angle) {
        return (int) (y + rad * Math.sin(this.angle + angle));
    }

	public void paint(Graphics g) {
        int[][] spur = new int[2][4];
        double theta;
        
        spur[0][3] = x;
        spur[1][3] = y;

        g.setColor(color1);
        spur[0][2] = rotateX(rSizeL, 0);
        spur[1][2] = rotateY(rSizeL, 0);
        for (theta = 0.0; theta < Math.PI - 0.000001; theta += gearToothAngle) {
        	paintSpur(g, spur, rSizeH, rSizeL, theta, gearToothAngle);
        }
        g.fillArc(x - (int) rSizeL, y - (int) rSizeL, (int) (rSizeL * 2), (int) (rSizeL * 2),
                180 - (int) (angle / (2 * Math.PI) * 360), 180);
        g.setColor(color2);
        if (gearCount % 2 == 1) {
            spur[0][0] = rotateX(rSizeL - 1, theta - gearToothAngle / 2);
            spur[1][0] = rotateY(rSizeL - 1, theta - gearToothAngle / 2);
            g.fillPolygon(spur[0], spur[1], 4);
        }

        spur[0][2] = rotateX(rSizeL, theta);
        spur[1][2] = rotateY(rSizeL, theta);
        for (; theta < 2 * Math.PI - 0.000001; theta += gearToothAngle) {
        	paintSpur(g, spur, rSizeH, rSizeL, theta, gearToothAngle);
        }
        g.fillArc(x - (int) rSizeL, y - (int) rSizeL, (int) (rSizeL * 2), (int) (rSizeL * 2),
                360 - (int) (angle / (2 * Math.PI) * 360), 180);

        int nAxles = 14;
        if (rad < 14)
            nAxles = (int) rad;

        g.setColor(Color.black);
        g.fillArc(x - nAxles / 2, y - nAxles / 2, nAxles, nAxles, 0, 360);
        g.setColor(Color.darkGray);
        g.fillArc(x - (nAxles - 2) / 2, y - (nAxles - 2) / 2, (nAxles - 2), (nAxles - 2), 0,
                360);

    }
	
	private void paintSpur(Graphics g, int[][] spur, double rSizeH, double rSizeL, double theta, double gearToothAngle) {

        spur[0][0] = spur[0][2];
        spur[1][0] = spur[1][2];

        spur[0][1] = rotateX(rSizeH, theta + gearToothAngle / 2);
        spur[1][1] = rotateY(rSizeH, theta + gearToothAngle / 2);

        spur[0][2] = rotateX(rSizeL, theta + gearToothAngle);
        spur[1][2] = rotateY(rSizeL, theta + gearToothAngle);
        g.fillPolygon(spur[0], spur[1], 4);

	}
}