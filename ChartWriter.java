import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class ChartWriter
{
	private ArrayList<Point> point;
	private PrintWriter pw;
	private String today;
	
	/**
	 * For top ten
	 */
	public ChartWriter(String location, ArrayList<Point> point, String today, String suffix) throws Exception {
		this.today = today;
		this.point = point;
		pw = new PrintWriter(new FileWriter(location + "\\" + today + suffix + ".txt"));
	}
	
	public void exportData(int size) {
    	String[] date = new String[size];
    	
    	DateUtility dateUtility = new DateUtility(today);
    	
    	for(int i = size-1; i >= 0; i--){
    		date[i] = dateUtility.toString();
    		dateUtility.prevDay();
    	}
    	
    	for(int i = 0; i < size; i++){
    		if (i == 0) pw.print(date[i]);
    		else pw.print("," + date[i]);
    	}
    	
    	pw.println();
    	
    	for(int i = 0; i < point.size(); i++) {
    		Point p = point.get(i);
    		int counter = 0;
    		String result = p.tag;
    		for(int j = 0; j < size && counter < p.history.size(); j++){
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
