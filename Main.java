import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jfree.ui.RefineryUtilities;


public class Main
{
	public static void main(String[] args) throws Exception{
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please insert the data location (csv format)");
        String location = inputReader.readLine();
        String[] test = location.split("\\\\");
        String date = test[test.length-1].substring(0, 10);
        
		BufferedReader dataReader = new BufferedReader(new FileReader(location));
		Separator separator = new Separator();
		String temp = dataReader.readLine();
		
		/* save list of tag */
		HashMap<String,Tag> tag = new HashMap<String,Tag>(); // collection of tag
		while(temp != null){
			try{
				String forum = temp.substring(0,3); // forum
				Integer.parseInt(forum);
				separator.setString(temp.substring(4)); // string that will processed
				Set<String> result = separator.separate(); // list of tag from one row
				for (String s : result){
					if(tag.containsKey(s)){
						tag.get(s).counter++;
					} else {
						tag.put(s,new Tag(forum,1));
					}
				}
			} catch (Exception e){

			}
			temp = dataReader.readLine();
		}
		
		/* process new data */
		Database database = new Database(tag,date);
		database.getOldestDay();
		database.insert();
		database.calculate();
		ArrayList<Point> point = database.getPoint(); // top ten tag for chart
		database.close();
		
		dataReader.close();
		inputReader.close();
		
		/* chart */
		Chart topChart = new Chart("Top Ten", point, date);
		topChart.pack();
		RefineryUtilities.centerFrameOnScreen(topChart);
        topChart.setVisible(true);
        Chart allChart = database.chart;
        allChart.visualize(false, 650, date);
        allChart.pack();
        RefineryUtilities.centerFrameOnScreen(allChart);
        allChart.setVisible(true);
	}
}