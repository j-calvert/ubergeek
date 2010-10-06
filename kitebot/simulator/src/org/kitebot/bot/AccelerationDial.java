package org.kitebot.bot;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D.Float;
import java.text.DecimalFormat;

import javax.swing.event.ChangeEvent;

import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.plot.dial.DialPointer.Pin;
import org.jfree.chart.plot.dial.DialPointer.Pointer;
import org.kitebot.gear.GearColor;

public class AccelerationDial extends BaseDial {

	double[] prev;
	long last;

	public AccelerationDial(Float area) {
		super(area, 3);
		prev = new double[3];
	}

	protected void createPlot(DialPlot dialplot) {
		DialTextAnnotation dialtextannotation = new DialTextAnnotation("Acc");
		dialtextannotation.setFont(new Font("Dialog", 1, 14));
		dialtextannotation.setRadius(0.7D);
		dialplot.addLayer(dialtextannotation);

		StandardDialScale standarddialscale = new StandardDialScale(0D, 140D,
				-120D, -300D, 20D, 4);
		standarddialscale.setTickRadius(0.93D);
		standarddialscale.setTickLabelOffset(0.19D);
		standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		standarddialscale.setTickLabelPaint(Color.black);
		standarddialscale.setTickLabelFormatter(new DecimalFormat("#"));
		dialplot.addScale(0, standarddialscale);
		dialplot.mapDatasetToScale(0, 0);
		dialplot.mapDatasetToScale(1, 0);

		StandardDialScale standarddialscale1 = new StandardDialScale(-40D, 40D,
				-120D, -300D, 10D, 4);
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
	}

	@Override
	public void stateChanged(ChangeEvent changeevent) {
		double[] dp = (double[]) changeevent.getSource();
		long now = System.nanoTime();
		double delta = 1000000000d / (now - last);
		last = now;
		for (int i = 0; i < datasets.length; i++) {
			datasets[i].setValue((dp[i] - prev[i]) / delta);
			prev[i] = dp[i];
		}
	}

}
