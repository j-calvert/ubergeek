package org.kitebot.bot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialLayer;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.plot.dial.DialPointer.Pin;
import org.jfree.chart.plot.dial.DialPointer.Pointer;
import org.jfree.data.general.DefaultValueDataset;
import org.kitebot.gear.GearColor;

public class Dial extends Component implements ChangeListener {

	DefaultValueDataset dataset0;
	DefaultValueDataset dataset1;
	DefaultValueDataset dataset2;
	private JFreeChart jfreechart;

	public void stateChanged(ChangeEvent changeevent) {
		Double[] vals = (Double[]) changeevent.getSource();
		dataset0.setValue(vals[0]);
		dataset1.setValue(vals[1]);
		dataset2.setValue(vals[2]);
	}

	public Dial() {
		dataset0 = new DefaultValueDataset(10D);
		dataset1 = new DefaultValueDataset(10D);
		dataset2 = new DefaultValueDataset(50D);
		DialPlot dialplot = new DialPlot();
		dialplot.setView(0.0D, 0.0D, 1.0D, 1.0D);
		dialplot.setDataset(0, dataset0);
		dialplot.setDataset(1, dataset1);
		dialplot.setDataset(2, dataset2);

		StandardDialFrame standarddialframe = new StandardDialFrame();
		standarddialframe.setBackgroundPaint(Color.white);
		standarddialframe.setForegroundPaint(Color.black);
		dialplot.setDialFrame(standarddialframe);
		GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(
				255, 255, 255), new Point(), new Color(170, 170, 220));

		DialBackground dialbackground = new DialBackground(gradientpaint);
		dialplot.setBackground(dialbackground);
		DialTextAnnotation dialtextannotation = new DialTextAnnotation(
				"RPM");
		dialtextannotation.setFont(new Font("Dialog", 1, 14));
		dialtextannotation.setRadius(0.7D);
		dialplot.addLayer(dialtextannotation);

		StandardDialScale standarddialscale = new StandardDialScale(0D, 140D, -120D, -300D, 20D, 4);
		standarddialscale.setTickRadius(0.93D);
		standarddialscale.setTickLabelOffset(0.19D);
		standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		standarddialscale.setTickLabelPaint(Color.black);
		standarddialscale.setTickLabelFormatter(new DecimalFormat("#"));
		dialplot.addScale(0, standarddialscale);
		dialplot.mapDatasetToScale(0, 0);
		dialplot.mapDatasetToScale(1, 0);
		
		StandardDialScale standarddialscale1 = new StandardDialScale(-40D, 40D, -120D, -300D, 10D, 4);
		standarddialscale1.setTickRadius(0.5D);
		standarddialscale1.setTickLabelOffset(0.15D);
		standarddialscale1.setTickLabelFont(new Font("Dialog", 0, 10));
		standarddialscale1.setMajorTickPaint(GearColor.SUN.fgClr.darker());
		standarddialscale1.setMinorTickPaint(GearColor.SUN.fgClr.darker());
		standarddialscale1.setTickLabelFormatter(new DecimalFormat("#"));
		standarddialscale1.setTickLabelPaint(GearColor.SUN.fgClr.darker());
		dialplot.addScale(1, standarddialscale1);
		dialplot.mapDatasetToScale(2, 1);
		
		Pin pin = new Pin(2);
		pin.setRadius(0.45D);
		pin.setPaint(GearColor.SUN.fgClr);
		dialplot.addPointer(pin);

		Pointer pointer = new Pointer(0);
		pointer.setFillPaint(GearColor.OUTER_RING.fgClr);
		pointer.setOutlinePaint(GearColor.OUTER_RING.fgClr.darker());
		dialplot.addPointer(pointer);

		Pointer pointer1 = new Pointer(1);
		pointer1.setFillPaint(GearColor.ANNULUS.fgClr);
		pointer1.setOutlinePaint(GearColor.ANNULUS.fgClr.darker());
		dialplot.addPointer(pointer1);

		DialCap dialcap = new DialCap();
		dialcap.setRadius(0.1D);
		dialplot.setCap(dialcap);
		
        jfreechart = new JFreeChart(dialplot);
        jfreechart.setBackgroundPaint(Color.white);
	}

	@Override
	public void paint(Graphics g) {
		jfreechart.draw((Graphics2D) g, new Rectangle2D.Float(0,400,200, 200));
	}
	
	
}
