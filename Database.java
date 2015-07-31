import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Database {
	private Connection conn;
	private Statement stat;
	private ResultSet rs;
	private HashMap<String, Tag> tag;
	private Set<String> keys;
	private String today, oldestDay = null;

	private static final String HOST = "jdbc:hsqldb:hsql://localhost/xdb";
	private static final String USER = "SA";
	private static final String PASSWORD = "";
	private static final Integer MAXIMUM_CHARACTERS = 20;

	public static void migrate () throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		
		Connection conn = DriverManager.getConnection(HOST, USER, PASSWORD);
		
		Statement stat = conn.createStatement();
		
		stat.executeQuery("DROP TABLE rating IF EXISTS CASCADE");
		
		stat.executeQuery("DROP TABLE history IF EXISTS CASCADE");
		
		stat.executeQuery("CREATE TABLE RATING( "
									+ "tag varchar(50) primary key, "
									+ "forum char(3) not null, "
									+ "score double)");
		
		stat.executeQuery("CREATE TABLE HISTORY( "
									+ "tag varchar(50) not null, "
									+ "date date not null, "
									+ "counter int, "
									+ "probability double, "
									+ "primary key(tag,date), "
									+ "foreign key(tag) references rating(tag))"
						);
	}
	
	public Database(HashMap<String, Tag> tag, String today, Boolean init) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		conn = DriverManager.getConnection(HOST, USER, PASSWORD);

		this.stat = conn.createStatement();
		this.tag = tag;
		this.today = today;
		this.keys = tag.keySet();

		conn.setAutoCommit(false);
	}
	
	public int getTotalDate() throws Exception {
		rs = stat.executeQuery("SELECT COUNT (DISTINCT date) as total FROM HISTORY");

		return rs.next() ? rs.getInt("total") : 0;		
	}
	
	public int getTotal() throws Exception {
		rs = stat.executeQuery("SELECT SUM(counter) as total FROM HISTORY WHERE date = '" + today + "'");

		return rs.next() ? rs.getInt("total") : 0;
	}

	public int getCounter(String key) throws Exception {
		rs = stat.executeQuery("SELECT counter FROM HISTORY where tag = '"+ key + "'" + " and date = '" + today + "'");

		return rs.next() ? rs.getInt("counter") : 0;
	}

	public double getMaxProb(String key) throws Exception {
		rs = stat.executeQuery("SELECT MAX(probability) as maxProb FROM HISTORY WHERE tag = '" + key + "'" + " and date < '" + today + "'" );

		return rs.next() ? rs.getDouble("maxProb") : 0;
	}

	/* KL Divergence */
	public double getScore(String key) throws Exception {
		rs = stat.executeQuery("SELECT score FROM RATING where tag = '" + key + "'");

		return rs.next() ? rs.getDouble("score") : 0;
	}

	public void commit() throws Exception {
		conn.commit();
	}

	public void rollback() throws Exception {
		conn.rollback();
	}

	public void close() throws Exception {
		stat.close();
		conn.close();
	}

	public void markOldestDay() throws Exception {
		rs = stat.executeQuery("SELECT MIN(date) as minDate FROM HISTORY");
		if (rs.next()) {
			oldestDay = rs.getString("minDate");
		}
	}

	public void deleteOldestDay() throws Exception {
		if (oldestDay != null) {
			stat.execute("DELETE FROM HISTORY WHERE date = '" + oldestDay + "'");
		}
	}

	/** Insert new data to HISTORY */
	public void insert() throws Exception {

		for (String key : keys) { 
			/* length filtering */
			if (key.length() < MAXIMUM_CHARACTERS) { 	
				Tag value = tag.get(key);
				
				rs = stat.executeQuery("SELECT tag FROM RATING WHERE tag = '" + key + "'");
				
				if (!rs.next()) {
					/* if key isn't exist yet */
					stat.execute("INSERT INTO RATING VALUES('" + key + "','" + value.forum + "',0)");
				}
				stat.execute("INSERT INTO HISTORY VALUES('" + key + "','" + today + "'," + value.counter + ",0)"); // if key already exist
			}
		}
	}

	/** Calculate and update score (KL divergence) */
	public void updateScores() throws Exception {
		
		int total = getTotal();

		/* decay the score exponentially */
		rs = stat.executeQuery("UPDATE RATING SET score = score/2.0");

		for (String key : keys) {
			rs = stat.executeQuery("SELECT date, counter, probability FROM HISTORY where tag = '"+ key + "'");

			int counter    = getCounter(key);
			double maxProb = getMaxProb(key);
			double score   = getScore(key);

			/* count the probability */
			double prob = (double) counter / total;
			
			if (maxProb < 1e-7) {
				maxProb = 1.0 / total;
			}
			
			if (prob < 1e-7) {
				prob = 1.0 / total;
			}
			
//			System.out.println(maxProb + " & " + prob + " = " + prob*Math.log(prob/maxProb));

			/* update KL Divergence */
			score += prob * Math.log(prob / maxProb);

			stat.execute("UPDATE RATING SET score = " + score + " WHERE tag = '" + key + "'");
		}
	}

	/** Calculate and update probabilities */
	public ArrayList<Point> updateProbabilities() throws Exception {

		ArrayList<Point> result = new ArrayList<Point>();
		
		int total = getTotal();

		/* counter tag for today */
		int counter = 1;

		for (String key : keys) {
			counter = getCounter(key);
			
			
			/* fill Array of Points */
			rs = stat.executeQuery("SELECT date, counter, probability FROM HISTORY where tag = '"+ key +"'");
			ArrayList<History> history = new ArrayList<History>(); // history for chart
			
			while(rs.next()){
				String date = rs.getString("date");
				int counterTag = rs.getInt("counter");
				if(date.equals(today)){
					counter = counterTag;
				}
				history.add(new History(date,counterTag));
			}
			result.add(new Point(key,history)); // add all tags to chart
			
			
			/* update the probability */
			double prob = (double) counter / total;
			stat.execute("UPDATE HISTORY SET probability = " + prob + " WHERE date = '" + today + "' and tag = '" + key + "'");
		}
		
		return result;
	}
	
	public TopTenResult getTopTen() throws Exception {
		 
		ArrayList<Point> points = new ArrayList<Point>();
		ArrayList<Data>  data   = new ArrayList<Data>();
		
		rs = stat.executeQuery("SELECT tag,forum,probability,score FROM RATING r, HISTORY h" + " WHERE h.tag = r.tag and date = '" + today + "' ORDER BY score desc limit 10");
		
		while(rs.next()){
			String tag   = rs.getString("tag");
			String forum = rs.getString("forum");
			double prob  = rs.getDouble("probability");
			double score = rs.getDouble("score");
			
			/* add coordinate for chart */
			ResultSet temp = stat.executeQuery("SELECT date,counter FROM HISTORY" + " WHERE tag = '"+ tag + "'");
			ArrayList<History> h = new ArrayList<History>();
			while(temp.next()){
				h.add(new History(temp.getString("date"),temp.getInt("counter")));
			}
			if (h.size() > 0) {
				points.add(new Point(rs.getString("tag"),h));
			}
			
			/* comparison probability */
			temp = stat.executeQuery("SELECT MAX(probability) as maxProb FROM HISTORY WHERE tag = '" + tag + "' and date < '" + today + "'");
			
			double max_prob = temp.next() ? temp.getDouble("maxProb") : 0;
			
			data.add(new Data(tag,forum,prob,score,max_prob));
		}
		
		return new TopTenResult(points,data);
	}
}

class TopTenResult {
	ArrayList<Point> points;
	ArrayList<Data>    data;
	
	public TopTenResult (ArrayList<Point> points, ArrayList<Data> data) {
		this.points = points;
		this.data   = data;
	}
}

class Data {
	public String tag;
	public String forum;
	public double prob;
	public double score;
	public double max_prob;
	
	public Data(String tag, String forum, double prob, double score, double max_prob) {
		this.tag = tag;
		this.forum = forum;
		this.prob = prob;
		this.score = score;
		this.max_prob = max_prob;
	}
}
