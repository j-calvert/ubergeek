import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// Thanks & ref to http://users.ecs.soton.ac.uk/msn/book/new_demo/corners/

public class Corners extends JApplet {

	Image edgeImage, accImage, outputImage;
	MediaTracker tracker = null;
	PixelGrabber grabber = null;
	int width = 0, height = 0;
	String fileNames[] = { "ms.png", "bmanpan.jpg", "frames.jpg" };
	Timer timer;

	private static final GridBagConstraints gbc;
	private static final int NUM_IMAGES = 4;
	static {
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
	}

	// slider constraints
	static int TH_MIN = 0;
	static int TH_MAX = 200;
	static int TH_INIT = 40;
	double threshold = (double) TH_INIT;
	int imageNumber = 0;
	static int progress = 0;

	Image image[] = new Image[fileNames.length];
	JProgressBar progressBar;
	JPanel selectionPanel, controlPanel, imagePanel, progressPanel;
	JLabel modeLabel, comboLabel, sigmaLabel, thresholdLabel, processingLabel;
	List<JLabel> imageLabels = new ArrayList<JLabel>();
	JSlider thresholdSlider;
	JButton thresholding;
	JComboBox imSel;
	static Harris harrisOp;

	// static Image edges;

	// Applet init function

	public void init() {
		tracker = new MediaTracker(this);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		for (int i = 0; i < fileNames.length; i++) {
			String imgFile = "/home/jeremyc/workspace/ubergeek/framey/img/"
					+ fileNames[i];
			image[i] = toolkit.getImage(imgFile);
			image[i] = image[i].getScaledInstance(512, 512, Image.SCALE_FAST);
			// System.out.println("height: " + image[i].getHeight(new
			// Canvas()));
			tracker.addImage(image[i], i);
		}
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			System.out.println("error: " + e);
		}
		Container cont = getContentPane();
		cont.removeAll();
		cont.setBackground(Color.black);
		cont.setLayout(new BorderLayout());
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 4, 15, 0));
		controlPanel.setBackground(new Color(192, 204, 226));
		imagePanel = new JPanel();
		imagePanel.setBackground(new Color(192, 204, 226));
		progressPanel = new JPanel();
		progressPanel.setBackground(new Color(192, 204, 226));
		progressPanel.setLayout(new GridLayout(2, 1));
		comboLabel = new JLabel("IMAGE");
		comboLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(comboLabel);
		modeLabel = new JLabel(("K = " + ((double) TH_INIT / 1000)));
		modeLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(modeLabel);
		processingLabel = new JLabel("Processing...");
		processingLabel.setHorizontalAlignment(JLabel.LEFT);
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true); // get space for the string
		progressBar.setString(""); // but don't paint it
		progressPanel.add(processingLabel);
		progressPanel.add(progressBar);
		width = image[imageNumber].getWidth(null);
		height = image[imageNumber].getHeight(null);
		System.out.println("(w, h) = (" + width + ", " + height + ")");
		imSel = new JComboBox(fileNames);
		imageNumber = imSel.getSelectedIndex();
		imSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageNumber = imSel.getSelectedIndex();
				imageLabels.get(0).setIcon(new ImageIcon(image[imageNumber]));
				processImage();
			}
		});

		controlPanel.add(imSel, BorderLayout.PAGE_START);
		timer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				progressBar.setValue(harrisOp.getProgress());
			}
		});

		for (int i = 0; i < NUM_IMAGES; i++) {
			JLabel imgLabel = new JLabel("Image " + i, new ImageIcon(
					image[imageNumber]), JLabel.CENTER);
			imgLabel.setVerticalTextPosition(JLabel.BOTTOM);
			imgLabel.setHorizontalTextPosition(JLabel.CENTER);
			imgLabel.setForeground(Color.blue);
			imageLabels.add(imgLabel);
			imagePanel.add(imgLabel, gbc);
		}
		thresholdSlider = new JSlider(JSlider.HORIZONTAL, TH_MIN, TH_MAX,
				TH_INIT);
		thresholdSlider.addChangeListener(new thresholdListener());
		thresholdSlider.setMajorTickSpacing(50);
		thresholdSlider.setMinorTickSpacing(10);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setBackground(new Color(192, 204, 226));
		controlPanel.add(thresholdSlider);
		cont.add(controlPanel, BorderLayout.NORTH);

		cont.add(imagePanel, BorderLayout.CENTER);

		cont.add(progressPanel, BorderLayout.SOUTH);

		processImage();

	}

	class thresholdListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				threshold = source.getValue();
				modeLabel.setText("K = " + ((double) source.getValue() / 1000));
				processImage();
			}
		}
	}

	private void processImage() {
		final int pixels[] = new int[width * height];
		PixelGrabber grabber = new PixelGrabber(image[imageNumber], 0, 0,
				width, height, pixels, 0, width);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		progressBar.setMaximum(width - 4);
		processingLabel.setText("Processing...");
		thresholdSlider.setEnabled(false);
		imSel.setEnabled(false);
		harrisOp = new Harris();
		timer.start();

		new Thread() {
			public void run() {
				// edgedetector.init(orig,width,height);
				// orig=edgedetector.process();
				// edges = image[imageNumber];//createImage(new
				// MemoryImageSource(width, height, orig, 0, width));
				harrisOp.init(pixels, width, height, threshold / 1000);
				int[] morePixels = harrisOp.process();
				final Image output = createImage(new MemoryImageSource(width,
						height, morePixels, 0, width));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						imageLabels.get(1).setIcon(new ImageIcon(output));
						// origLabel.setIcon(new ImageIcon(createImage(new
						// MemoryImageSource(width, height,
						// edgedetector.process(), 0, width))));
						processingLabel.setText("Done");
						thresholdSlider.setEnabled(true);
						imSel.setEnabled(true);
					}
				});
			}
		}.start();
	}
}
