package scanner;
/**

 * (./) BlobDetection.java, 03/05/08
 * (by) cousot stephane @ http://www.ubaa.net/
 * (cc) some right reserved
 *
 * Sample file for the "OpenCV" project.
 * Use ESC key to close the program properly.
 * Press SPACE BAR to record background image.
 *
 * This sample is released under a Creative Commons Attribution 3.0 License
 * ‹ http://creativecommons.org/licenses/by/3.0/ ›
 */
 
 
 
import hypermedia.video.Blob;
import hypermedia.video.OpenCV;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.MemoryImageSource;


 
public class BlobDetection extends Frame implements Runnable {
		private static final long serialVersionUID = 1L;
	// program execution frame rate (millisecond)
	final int FRAME_RATE  = 1000/30;
	// threshold value for removing noises
	final float THRESHOLD = 250f; 
	
	
	OpenCV cv = null;	// OpenCV Object
	Thread t  = null;	// the program thread
	
	
	
	// the input video stream image
	Image frame	 = null;
	Blob[] blobs = null;
	
	
	/**
	 * Setup Frame and Object(s).
	 */
	BlobDetection() {
		
		super( "Blob Detection Sample" );
		
		
		// OpenCV setup
		cv = new OpenCV();
		cv.capture( 640, 480);
		cv.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );
		
		
		// frame setup
		this.setBounds( 100, 100, cv.width, cv.height );
		this.setBackground( Color.BLACK );
		this.setVisible( true );
		this.addKeyListener(
			new KeyAdapter() {
				public void keyReleased( KeyEvent e ) { 
					if ( e.getKeyCode()==KeyEvent.VK_ESCAPE ) { // ESC : release OpenCV resources
						t = null;
						cv.dispose();
						System.exit(0);
					}
					if ( e.getKeyCode()==KeyEvent.VK_SPACE ) // SPACE : record background
						cv.remember();
				}
			}
		);
		
		
		// start running program
		t = new Thread( this );
		t.start();
		
	}
	
	
	
	/**
	 * Release OpenCV resources.
	 */
	public void stop() {
		t = null;
		cv.dispose();
	}
	
	
	/**
	 * Draw video frame and each detected faces area.
	 */
	public void paint( Graphics g ) {
		if ( frame==null || blobs==null ) return;
		
		// draw image
		//g.drawImage( frame, 0, 0, null );
		
		// draw blobs
		for( Blob b : blobs ) {
		
			// define blob's contour
			Polygon shape = new Polygon();
			for( Point p : b.points ) shape.addPoint( p.x, p.y );
			
			// fill blob
			g.setColor( b.isHole ? Color.RED : Color.BLUE );
			g.fillPolygon( shape );
		}
	}
	
	
	
	
	/**
	 * Execute this sample.
	 */
	public void run() {
		while( t!=null && cv!=null ) {
			try {
				Thread.sleep( FRAME_RATE * 10);
				cv.capture(640, 480);
				// grab image from video stream
				cv.read();
				//cv.flip( OpenCV.FLIP_HORIZONTAL );
				
				// create a new image from cv pixels data
				MemoryImageSource mis = new MemoryImageSource( cv.width, cv.height, cv.pixels(), 0, cv.width );
				frame = createImage( mis );
				
				// prepare image for detection
				cv.convert(12);
				cv.absDiff();
				cv.threshold( THRESHOLD );
				
				// detect blobs
				blobs = cv.blobs( 30, cv.width*(cv.height/2), 100, true, OpenCV.MAX_VERTICES*4 );
				
				// of course, repaint
				repaint();
			}
			catch( InterruptedException e ) {;}
		}
	}
	
	
	/**
	 * Main method.
	 * @param String[]	list of arguments's user passed from the console to this program
	 */
	public static void main( String[] args ) {
		System.out.println( "\nOpenCV blob detection sample" );
		System.out.println( "PRESS SPACE BAR TO RECORD BACKGROUND IMAGE\n" );
		new BlobDetection();
	} 

}