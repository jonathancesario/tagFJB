import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Database
{
	Connection conn;
	Statement stat;
	ResultSet rs;
	HashMap<String,Tag> tag;
	Set<String> keys;
	String today, oldestDay;
	ChartWriter chart;
	
	public Database(HashMap<String,Tag> tag, String today) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
        conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "SA", "");
        conn.setAutoCommit(false);
        stat = conn.createStatement();
        this.tag = tag;
        this.today = today;
        keys = tag.keySet();
        chart = new ChartWriter(today);
	}
	
	public void getOldestDay() throws Exception {
		rs = stat.executeQuery("SELECT MIN(date) as minDate FROM HISTORY");
		if(rs.next()){
			oldestDay = rs.getString("minDate");
		}
	}
	
	/**
	 * Insert new data to HISTORY
	 */
	public void insert() throws Exception {
		for(String key: keys){
			if(key.length() < 25){ // ignore the symbols
				Tag value = tag.get(key);
				rs = stat.executeQuery("SELECT tag FROM RATING WHERE tag = '"+key+"'");
				if(!rs.next()){
					stat.execute("INSERT INTO RATING VALUES('"+key+"','"+value.forum+"',0)"); // if key isn't exist yet
				}
				stat.execute("INSERT INTO HISTORY VALUES('"+key+"','"+today+"',"+value.counter+",0)"); // if key already exists
			}
		}
	}
	
	/**
	 * Calculate probability and score (KL divergence)
	 */
	public void calculate() throws Exception {
		rs = stat.executeQuery("SELECT SUM(counter) as total FROM HISTORY WHERE date = '"+ today +"'");
		double total = 1;
		if(rs.next()){
			total = rs.getInt("total");
		}
		rs = stat.executeQuery("UPDATE RATING SET score = score/2.0");
		for(String key: keys){
			/* max probability last 7 days */
			double maxProb = 1;
			rs = stat.executeQuery("SELECT MAX(probability) as maxProb FROM HISTORY WHERE tag = '"+ key +"'");
			if(rs.next()){
				maxProb = rs.getDouble("maxProb");
			}
			
			rs = stat.executeQuery("SELECT date, counter, probability FROM HISTORY where tag = '"+ key +"'");
			double counter = 1; // counter tag for today
			ArrayList<History> history = new ArrayList<History>(); // history for chart
			/* history for a week */
			while(rs.next()){
				String date = rs.getString("date");
				int counterTag = rs.getInt("counter");
				if(date.equals(today)){
					counter = counterTag;
				}
				history.add(new History(date,counterTag));
			}
			chart.point.add(new Point(key,history)); // add every tag to chart
			
			/* probability */
			double prob = counter/total;
			stat.execute("UPDATE HISTORY SET probability = "+ prob
					+ " WHERE date = '" + today + "' and tag = '" + key + "'");
			
			/* KL divergence */
			if(maxProb != 0){
				double score = prob*Math.log(prob/maxProb);
				stat.execute("UPDATE RATING SET score = "+score+" WHERE tag = '"+key+"'");
			}
		}
		stat.execute("DELETE FROM HISTORY WHERE date = '"+oldestDay+"'"); // delete oldest day
		chart.exportData();
	}
	
	/**
	 * Get coordinates of Top Ten Tag
	 * @return list of Top Ten Tag
	 */
	public ArrayList<Point> getTopTen() throws Exception {
		PrintWriter pw = new PrintWriter(new FileWriter(
				"C:\\Users\\gdplabs.intern\\Desktop\\TrendFJB\\prediction\\" + today + ".txt"));
		pw.println("Hot Tag FJB");
		ArrayList<Point> result = new ArrayList<Point>();
		rs = stat.executeQuery("SELECT tag,forum,probability,score FROM RATING r, HISTORY h"
				+ " WHERE h.tag = r.tag and date = '" + today + "' ORDER BY score desc limit 10");
		int counter = 1;
		DecimalFormat df = new DecimalFormat("#0.0000000000"); // format 10 digits decimal
		while(rs.next()){
			String tag = rs.getString("tag");
			String forum = rs.getString("forum");
			double prob = rs.getDouble("probability");
			System.out.print(counter+". "+tag+" - "+forum+" - "+prob);
			pw.print(counter+". "+tag+" - "+forum+" - "+prob);
			
			/* add coordinate for chart */
			ResultSet temp = stat.executeQuery("SELECT date,counter,probability FROM HISTORY where tag = '"+rs.getString("tag")+"'");
			ArrayList<History> h = new ArrayList<History>();
			while(temp.next()){
				h.add(new History(temp.getString("date"),temp.getInt("counter")));
			}
			result.add(new Point(rs.getString("tag"),h));
			
			/* comparison probability */
			temp = stat.executeQuery("SELECT MAX(probability) as maxProb FROM HISTORY WHERE tag = '" + tag
					+ "' and date < '" + today + "'");
			if(temp.next()){
				prob = temp.getDouble("maxProb");
			}
			String format = df.format(prob);
			System.out.println("; maxProb=" + format);
			pw.println("; maxProb=" + format);
			counter++;
		}
		System.out.println();
		pw.close();
		return result;
	}
	
	public void commit(String commit) throws Exception {
		if(commit.equals("Y")){
			conn.commit();
		} else {
			conn.rollback();
		}
	}
	
	public void close() throws Exception {
		stat.close();
		conn.close();
	}
}
