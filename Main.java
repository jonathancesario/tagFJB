import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


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
		ArrayList<Point> point = database.getTopTen(); // top ten tag for chart
		
		/* export data chart */
		ChartWriter chart = new ChartWriter(point,date);
		chart.exportData();
		
		/* generate chart */
		Generator topTen = new Generator(true, 
				"C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\" + date + "topTen.txt");
		topTen.createChart();
		Generator all = new Generator(false, 
				"C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\" + date + "all.txt");
		all.createChart();
		
		/* commit */
		System.out.println("Commit? Y/N");
		String commit = inputReader.readLine();
		database.commit(commit);
		
		database.close();
		dataReader.close();
		inputReader.close();
	}
}