import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;


public class Chart extends ApplicationFrame
{
    public Chart(final String title, ArrayList<Point> point) {
        super(title);
        final XYDataset dataset = createDataset(point);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 450));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Hot Tag FJB",
            "Date", "Counter",
            dataset,
            true,
            true,
            false
        );

        chart.setBackgroundPaint(Color.white);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            //rr.setPlotShapes(true);
            rr.setShapesFilled(true);
            rr.setItemLabelsVisible(true);
        }
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd-MM"));
        
        return chart;
    }
    
    private XYDataset createDataset(ArrayList<Point> point) {
    	TimeSeries tag1 = new TimeSeries(point.get(0).tag, Day.class);
    	tag1 = insertHistory(tag1, point.get(0));
    	TimeSeries tag2 = new TimeSeries(point.get(1).tag, Day.class);
    	tag2 = insertHistory(tag2, point.get(1));
    	TimeSeries tag3 = new TimeSeries(point.get(2).tag, Day.class);
    	tag3 = insertHistory(tag3, point.get(2));
    	TimeSeries tag4 = new TimeSeries(point.get(3).tag, Day.class);
    	tag4 = insertHistory(tag4, point.get(3));
    	TimeSeries tag5 = new TimeSeries(point.get(4).tag, Day.class);
    	tag5 = insertHistory(tag5, point.get(4));

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(tag1);
        dataset.addSeries(tag2);
        dataset.addSeries(tag3);
        dataset.addSeries(tag4);
        dataset.addSeries(tag5);
        dataset.setDomainIsPointsInTime(true);

        return dataset;
    }
    
    private TimeSeries insertHistory(TimeSeries tag, Point point){
    	TimeSeries result = tag;
    	for(int i = 0; i < point.history.size(); i++){
    		History history = point.history.get(i);
    		int day = Integer.parseInt(history.date.substring(8,10));
    		int month = Integer.parseInt(history.date.substring(5,7));
    		int year = Integer.parseInt(history.date.substring(0,4)); 
    		result.add(new Day(day,month,year), history.counter);
    	}
    	return result;
    }
}