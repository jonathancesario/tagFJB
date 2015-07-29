import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jfree.ui.RefineryUtilities;


/**
 * Generate a chart to visualize prediction
 */
public class ChartGenerator
{
	public static void main (String[] args) throws Exception {
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Please insert the directory and day that will be visualized (YYYY-mm-DD)");
		String location = inputReader.readLine();
		
		Generator topTen = new Generator(true, location + "topTen.txt");
		topTen.createChart();
		Generator all = new Generator(false, location + "all.txt");
		all.createChart();
		inputReader.close();
	}
}


/**
 * Process the data
 */
class Generator
{
	Chart chart;
	BufferedReader dataReader;
	ArrayList<Point> point;
	boolean legend;
	
	public Generator(boolean legend, String location) throws Exception {
		dataReader = new BufferedReader(new FileReader(location));
		point = new ArrayList<Point>();
		this.legend = legend;
		parse();
	}
	
	/**
	 * Make array for chart input from text
	 */
	private void parse() throws Exception {
		String[] date = dataReader.readLine().split(",");
		String temp = dataReader.readLine();
		
		while(temp != null){
			String[] splitter = temp.split(",");
			ArrayList<History> history = new ArrayList<History>();
			for(int i = 1; i < splitter.length; i++){
				history.add(new History(date[i-1],Integer.parseInt(splitter[i])));
			}
			point.add(new Point(splitter[0],history));
			temp = dataReader.readLine();
		}
	}
	
	public void createChart() throws Exception {
		if(legend) {
			chart = new Chart("Top Ten",true,point);
		} else {
			chart = new Chart("Comparison Chart",false,point);
		}
		chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
		dataReader.close();
	}
}