import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

public class Chart extends ApplicationFrame {
	private static final long serialVersionUID = 1L;
	TimeSeriesCollection dataset;

	public Chart(final String title, boolean legend, ArrayList<Point> p) {
		super(title);
		dataset = new TimeSeriesCollection();
		createDataset(p);
		if (legend) {
			visualize(legend, 350);
		} else {
			visualize(legend, 650);
		}
	}

	private void visualize(boolean legend, int width) {
		final XYDataset data = dataset;
		final JFreeChart chart = createChart(data, legend);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, width));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
	}

	private JFreeChart createChart(final XYDataset dataset, boolean legend) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Hot Tag FJB", "Date", "Counter", dataset, legend, true, false);
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
	 * Create dataset for top ten Tag
	 */
	private void createDataset(ArrayList<Point> p) {
		for (int i = 0; i < p.size(); i++) {
			addDataset(p.get(i));
		}
	}

	/**
	 * Add TimeSeries to dataset
	 */
	private void addDataset(Point p) {
		TimeSeries t = new TimeSeries(p.tag);
		t = insertHistory(t, p);
		dataset.addSeries(t);
	}

	/**
	 * Get date and counter history from a tag
	 */
	private TimeSeries insertHistory(TimeSeries tag, Point p) {
		TimeSeries result = tag;

		for (int i = 0; i < p.history.size(); i++) {
			History history = p.history.get(i);
			String[] dates = history.date.split("-");
			int day = Integer.parseInt(dates[2]);
			int month = Integer.parseInt(dates[1]);
			int year = Integer.parseInt(dates[0]);
			result.add(new Day(day, month, year), history.counter);
		}
		return result;
	}
}