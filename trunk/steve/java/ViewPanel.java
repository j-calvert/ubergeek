import hypermedia.video.Blob;
import hypermedia.video.OpenCV;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.MemoryImageSource;

public class ViewPanel extends Panel implements Runnable{
    private OpenCV cv = null;
    private Image img = null;
    private Blob[] blobs = new Blob[0];
    private Thread t = null;
    final float THRESHOLD = 80f; //Adjust this as needed
        
    public ViewPanel() {
            super();
            t = new Thread( this );

    }
    
    public void remember() {
    	cv.remember();
    }

    @Override
    public void paint(Graphics g){
        
        if (this.img != null){
//        	synchronized(img) {
            g.drawImage(img, 0, 0, this);
            
            for( Blob b : blobs ) {
                // define blob's contour
                Polygon shape = new Polygon();
                for( Point p : b.points ) shape.addPoint( p.x, p.y );
                
                g.setColor( b.isHole ? Color.RED : Color.BLUE );
                g.fillPolygon( shape );
            }
        }
//        }
    }

    public void run() {
        cv = new OpenCV();
        cv.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );
        cv.capture( 320, 240 );
        
        while( t!=null && cv!=null ) {
            
            cv.read();
//            cv.flip( OpenCV.FLIP_HORIZONTAL );
            
            
//            cv.convert(12);
            cv.absDiff();
            MemoryImageSource mis = new MemoryImageSource( cv.width, cv.height, cv.pixels(), 0, cv.width );
            img = createImage( mis );
            cv.threshold( THRESHOLD );
            
            blobs = cv.blobs( 30, cv.width*(cv.height/2), 100, true, 5000);
            repaint();
            try {
				Thread.sleep(1000 / 30);
			} catch (InterruptedException e) { }
        }
        
    }
    
    public void stop() {
        t = null;
        cv.dispose();
    }
    public void start() {
        this.t.start();
    }
    
} 