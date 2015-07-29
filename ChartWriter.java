import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class ChartWriter
{
	ArrayList<Point> point;
	PrintWriter pw;
	String today; 
	
	/**
	 * For all chart
	 */
	public ChartWriter(String today) throws Exception {
		this.today = today;
		point = new ArrayList<Point>();
    	pw = new PrintWriter(new FileWriter(
				"C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\" + today + "all.txt"));
	}
	
	/**
	 * For top ten
	 */
	public ChartWriter(ArrayList<Point> point, String today) throws Exception {
		this.today = today;
		this.point = point;
		pw = new PrintWriter(new FileWriter(
				"C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\" + today + "topTen.txt"));
	}
	
	public void exportData() {
    	String[] date = new String[4];
    	date[3] = today;
    	DateUtility dateUtility = new DateUtility(today);
    	for(int i = 2; i >= 0; i--){
    		date[i] = dateUtility.prevDay(1).toString();
    	}
    	pw.print(date[0]);
    	for(int i = 1; i < 4; i++){
    		pw.print("," + date[i]);
    	}
    	pw.println();
    	for(int i = 0; i < point.size(); i++) {
    		Point p = point.get(i);
    		int counter = 0;
    		String result = p.tag;
    		for(int j = 0; j < 8 && counter < p.history.size(); j++){
				if(p.history.get(counter).date.equals(date[j])){
					result += "," + p.history.get(counter).counter;
					counter++;
    			} else {
    				result += ",0";
    			}
			}
    		pw.println(result);
    	}
    	pw.close();
    }
}
