package org.kitebot.bot.unused;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D.Float;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.kitebot.gear.GearColor;

public class ScrollingPlots extends Component implements ChangeListener {

    private final Float area;
	private static final int SUBPLOT_COUNT = 3;
    private static final int RESOLUTION = 500;
    private long lastPlot = 0l;

    private TimeSeriesCollection[] datasets = new TimeSeriesCollection[SUBPLOT_COUNT];
    private JFreeChart chart;

    public ScrollingPlots(Float area) {
    	this.area = area;
        
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        
        for (int i = 0; i < SUBPLOT_COUNT; i++) {
            TimeSeries series = new TimeSeries(GearColor.values()[i].toString());

            datasets[i] = new TimeSeriesCollection(series);
            NumberAxis rangeAxis = new NumberAxis();
            rangeAxis.setAutoRangeIncludesZero(false);
            StandardXYItemRenderer renderer = new StandardXYItemRenderer();
            renderer.setSeriesPaint(0, GearColor.values()[i].fgClr);
			XYPlot subplot = new XYPlot(this.datasets[i], null, rangeAxis, renderer);
            subplot.setDomainGridlinePaint(Color.lightGray);
            subplot.setRangeGridlinePaint(Color.lightGray);
            subplot.setBackgroundPaint(Color.white);
            plot.add(subplot);
        }
        chart = new JFreeChart(null, null, plot, false);
        chart.setBackgroundPaint(Color.white);
        
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(30000.0);  // 60 seconds
    }

	public void stateChanged(ChangeEvent changeevent) {
		Datapoint dp = (Datapoint) changeevent.getSource();
		if(dp.clock > lastPlot + RESOLUTION) {
		Millisecond t = new Millisecond();
			for(int i = 0; i < SUBPLOT_COUNT; i++) {
				datasets[i].getSeries(0).add(t, dp.vals[i]);
			}
		}
	}
	
	@Override
	public void paint(Graphics g) {
		chart.draw((Graphics2D) g, area);
	}
}
