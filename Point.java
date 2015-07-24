import java.util.ArrayList;


public class Point
{
	String tag;
	ArrayList<History> history;
	
	public Point(String tag, ArrayList<History> history){
		this.tag = tag;
		this.history = history;
	}
}

class History
{
	String date;
	int counter;
	
	public History(String date, int counter){
		this.date = date;
		this.counter = counter;
	}
}