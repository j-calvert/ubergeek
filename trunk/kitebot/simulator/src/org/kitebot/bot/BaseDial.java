package org.kitebot.bot;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.data.general.DefaultValueDataset;

public abstract class BaseDial extends Component implements ChangeListener {

	private final Rectangle2D.Float area;
	private final DefaultValueDataset[] datasets;
	private final JFreeChart chart;

	public BaseDial(Float area, int dialCount) {
		this.area = area;
		DialPlot dialplot = new DialPlot();
		datasets = new DefaultValueDataset[dialCount];
		dialplot.setView(0.0D, 0.0D, 1.0D, 1.0D);
		for (int i = 0; i < dialCount; i++) {
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

		createPlot(dialplot);

		chart = new JFreeChart(dialplot);
		chart.setBackgroundPaint(Color.white);
	}

	protected abstract void createPlot(DialPlot dialplot);

	@Override
	public void stateChanged(ChangeEvent changeevent) {
		double[] dp = (double[]) changeevent.getSource();
		for (int i = 0; i < datasets.length; i++) {
			datasets[i].setValue(dp[i]);
		}
	}

	@Override
	public void paint(Graphics g) {
		chart.draw((Graphics2D) g, area);
	}

}
