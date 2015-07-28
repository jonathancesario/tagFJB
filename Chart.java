import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;


public class Chart extends ApplicationFrame
{
	private static final long serialVersionUID = 1L;
	TimeSeriesCollection dataset, datasetProb;
	int counter;
	
	/**
	 * For all chart
	 */
    public Chart(final String title) {
        super(title);
        dataset = new TimeSeriesCollection();
        
    }
    
    /**
     * For top ten
     * @param point is the coordinate
     */
    public Chart(final String title, ArrayList<Point> point, String date) {
    	super(title);
    	dataset = new TimeSeriesCollection();
    	datasetProb = new TimeSeriesCollection();
    	createDataset(point);
    	try {
    		visualize(true, 350, date);
    	} catch (Exception e) {
    		
    	}
    }
    
    public void visualize(boolean legend, int width, String date) throws Exception {
    	final XYDataset data = dataset;
    	final JFreeChart chart = createChart(data, legend);
    	final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, width));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        if(legend){ // topTen
        	ChartUtilities.saveChartAsPNG(new File
        			("C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\"+date+"topTen.png"), chart, 500, width);
        } else { // all
        	ChartUtilities.saveChartAsPNG(new File
        			("C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\"+date+"all.png"), chart, 500, width);
        }
    }

    private JFreeChart createChart(final XYDataset dataset, boolean legend) {
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Hot Tag FJB",
            "Date", "Counter",
            dataset,
            legend,
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
        
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd-MM"));
        
        return chart;
    }
    
    /**
     * Add TimeSeries to dataset
     */
    public void addDataset(Point point) {
    	TimeSeries tagCounter = new TimeSeries(point.tag);
    	tagCounter = insertHistory(tagCounter,point,true);
    	dataset.addSeries(tagCounter);
    }
    
    /**
     * Get date and counter history from a tag
     */
    private TimeSeries insertHistory(TimeSeries tag, Point point, boolean isCounter){
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
    
    /**
     * Create dataset for top ten Tag
     */
    private void createDataset(ArrayList<Point> point) {
    	for(int i = 0; i < point.size(); i++){
    		addDataset(point.get(i));
    	}
    }
}