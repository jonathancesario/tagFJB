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
    	String[] date = new String[8];
    	boolean done = false;
    	for(int i = 0; i < point.size() && !done; i++){
    		Point p = point.get(i);
			if(p.history.size() == 8){ // tag appears every day
				done = true;
				for(int j = 0; j < p.history.size(); j++){
					date[j] = p.history.get(j).date;
					if(j == p.history.size()-1){
						pw.print(date[j]);
					} else {
						pw.print(date[j] + ",");
					}
				}
				pw.println();
			}
     	}
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
