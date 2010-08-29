package org.kitebot.bot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.text.DecimalFormat;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.plot.dial.DialPointer.Pin;
import org.jfree.chart.plot.dial.DialPointer.Pointer;
import org.jfree.data.general.DefaultValueDataset;
import org.kitebot.gear.GearColor;

public class AltitudeDial extends Component implements ChangeListener {

	private static final int DIAL_COUNT = 2;

	Rectangle2D.Float area;
	private DefaultValueDataset[] datasets = new DefaultValueDataset[DIAL_COUNT];
	private JFreeChart chart;

	public AltitudeDial(Float area) {
		this.area = area;
		DialPlot dialplot = new DialPlot();
		dialplot.setView(0.0D, 0.0D, 1.0D, 1.0D);
		for (int i = 0; i < DIAL_COUNT; i++) {
			datasets[i] = new DefaultValueDataset(0D);
			dialplot.setDataset(i, datasets[i]);
		}

		StandardDialFrame standarddialframe = new StandardDialFrame();
		standarddialframe.setBackgroundPaint(Color.white);
		standarddialframe.setForegroundPaint(Color.black);
		dialplot.setDialFrame(standarddialframe);
		GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(
				255, 255, 255), new Point(), new Color(170, 170, 220));

		DialBackground dialbackground = new DialBackground(gradientpaint);
		dialplot.setBackground(dialbackground);
		DialTextAnnotation dialtextannotation = new DialTextAnnotation("Line Length");
		dialtextannotation.setFont(new Font("Dialog", 1, 8));
		dialtextannotation.setRadius(0.7D);
		dialplot.addLayer(dialtextannotation);

		StandardDialScale standarddialscale = new StandardDialScale(0D, 500D,
				-120D, -300D, 100D, 4);
		standarddialscale.setTickRadius(0.93D);
		standarddialscale.setTickLabelOffset(0.19D);
		standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		standarddialscale.setTickLabelPaint(Color.black);
		standarddialscale.setTickLabelFormatter(new DecimalFormat("#"));
		dialplot.addScale(0, standarddialscale);
		dialplot.mapDatasetToScale(0, 0);

		StandardDialScale standarddialscale1 = new StandardDialScale(0D, 100D,
				-270D, -360D, 10D, 10);
		standarddialscale1.setTickRadius(0.5D);
		standarddialscale1.setTickLabelOffset(0.15D);
		standarddialscale1.setTickLabelFont(new Font("Dialog", 0, 10));
		standarddialscale1.setMajorTickPaint(Color.black);
		standarddialscale1.setMinorTickPaint(Color.gray);
		standarddialscale1.setTickLabelFormatter(new DecimalFormat("#"));
		standarddialscale1.setTickLabelPaint(GearColor.LINE.fgClr);
		dialplot.addScale(1, standarddialscale1);
		dialplot.mapDatasetToScale(1, 1);

		Pin pin = new Pin(1);
		pin.setRadius(0.45D);
		pin.setPaint(GearColor.LINE.fg());
		dialplot.addPointer(pin);

		Pointer pointer = new Pointer(0);
		pointer.setFillPaint(GearColor.LINE.fg());
		pointer.setOutlinePaint(GearColor.LINE.bg());
		dialplot.addPointer(pointer);

		DialCap dialcap = new DialCap();
		dialcap.setRadius(0.1D);
		dialplot.setCap(dialcap);

		chart = new JFreeChart(dialplot);
		chart.setBackgroundPaint(Color.white);
	}

	@Override
	public void stateChanged(ChangeEvent changeevent) {
		Double d = (Double) changeevent.getSource();
		for (int i = 0; i < DIAL_COUNT; i++) {
			datasets[i].setValue(d);
		}
	}

	@Override
	public void paint(Graphics g) {
		chart.draw((Graphics2D) g, area);
	}

}
