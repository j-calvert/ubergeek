import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import name.audet.samuel.javacv.CanvasFrame;
import name.audet.samuel.javacv.jna.highgui;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import name.audet.samuel.javacv.jna.cxcore.CvSize.ByValue;
import name.audet.samuel.javacv.jna.highgui.CvCapture;
import name.audet.samuel.javacv.jna.highgui.CvVideoWriter;

public class JavacvTest {

	public static void main(String[] args) throws Exception {
		CvCapture cvCapture = highgui.cvCreateCameraCapture(0);
		CvVideoWriter videoWriter = highgui.cvCreateVideoWriter("test.mp4", 0, 30.0, new ByValue(640, 480), 1);
		IplImage iplImage = highgui.cvQueryFrame(cvCapture);
//		CanvasFrame canvas = new CanvasFrame(false);
//		BufferStrategy strategy = canvas.getBufferStrategy();
//		canvas.setVisible(true);
		for(int j = 0; j < 200; j++) {
			int i = highgui.cvGrabFrame(cvCapture);
			if(i != 1) {
				System.err.println(i + " is not 1");
			}
//			System.out.println(i);
			iplImage = highgui.cvRetrieveFrame(cvCapture);
//			Thread.sleep(10);
//			BufferedImage image = iplImage.getBufferedImage();
//	        if (image == null)
//	            return;
//	        final int w = (int)Math.round(image.getWidth(null));
//	        final int h = (int)Math.round(image.getHeight(null));
//
//	        if (canvas.getWidth() != w || canvas.getHeight() != h) {
//	        	canvas.setCanvasSize(w, h);
//	        }
	        highgui.cvWriteFrame(videoWriter, iplImage);
//	        iplImage.getBufferedImage();
//	        Graphics2D g = canvas.acquireGraphics();
//	        boolean drawImage = g.drawImage(image, 0, 0, w, h, null);
//	        System.out.println(drawImage);
//	        canvas.releaseGraphics(g);
		}
		highgui.cvReleaseVideoWriter(videoWriter.pointerByReference());
	}
}
