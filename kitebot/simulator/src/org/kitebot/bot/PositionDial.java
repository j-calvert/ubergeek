package org.kitebot.bot;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D.Float;
import java.text.DecimalFormat;

import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.plot.dial.DialPointer.Pin;
import org.jfree.chart.plot.dial.DialPointer.Pointer;
import org.kitebot.gear.GearColor;

public class PositionDial extends BaseDial {

	public PositionDial(Float area) {
		super(area, 2);
	}

	protected void createPlot(DialPlot dialplot) {
		DialTextAnnotation dialtextannotation = new DialTextAnnotation(
				"Line Length");
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
	}
}
