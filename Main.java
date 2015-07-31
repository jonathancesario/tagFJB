import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Main {
	private static BufferedReader dataReader, inputReader;
	private static Separator separator;
	private static DateUtility date;
	private static Integer days;
	private static Boolean init;
	private static String location, predict_location;
	private static Database database;
	private static ArrayList<Point> points;

	public static void input() {
		Boolean succeed;

		do {
			try {
				succeed = true;
				
				System.out.println("Predict / Init / Migrate ? (P/I/M)");
				
				String command = inputReader.readLine();
				
				if (command.equalsIgnoreCase("M")) {
					System.out.println("Migrating...");
					Database.migrate();
					System.out.println("Migrated Successfully");
					succeed = false;
					continue;
				}
				
				init = (command.equalsIgnoreCase("I"));

				System.out.println("Please insert the data location (folder directory)");
				location = inputReader.readLine();

				System.out.println("Please insert the start date (yyyy-MM-dd)");
				date = new DateUtility(inputReader.readLine());
				
				if (!init) {
					System.out.println("Please insert the prediction result location (folder directory)");
					predict_location = inputReader.readLine();
				}

				dataReader = new BufferedReader(new FileReader(location + "\\" + date + ".csv"));
				
				separator = new Separator(location);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				succeed = false;
			}
		} while (!succeed);

		do {
			try {
				succeed = true;

				System.out.println("Please insert duration days (days)");
				days = Integer.parseInt(inputReader.readLine());

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				succeed = false;
			}
		} while (!succeed);
	}

	public static ArrayList<Point> inputProcess() throws Exception {
		HashMap<String, Tag> tag = new HashMap<String, Tag>();
		String temp = dataReader.readLine();

		/* save list of tags */
		while (temp != null) {
			try {
				String input[] = temp.split(",");

				String forum = input[0]; // forum_id
				String tags = input[1]; // raw tags

				/* string (tags) that will be processed */
				separator.setString(tags);

				/* process list of tags from one row */
				Set<String> result = separator.separate();
				for (String s : result) {
					if (tag.containsKey(s)) {
						tag.get(s).counter++;
					} else {
						tag.put(s, new Tag(forum, 1));
					}
				}
			} catch (Exception e) {}

			temp = dataReader.readLine();
		}
		
		System.out.println("data " + date + ".csv is going to be processed");
		
		/* process new data */
		database = new Database(tag, date.toString(), init);
		database.insert();
		
		System.out.println("data " + date + ".csv successfully inserted to database");
		
		return database.updateProbabilities();
	}
	
	public static void printResult () throws Exception {
		TopTenResult result = database.getTopTen(); 
		
		PrintWriter pw = new PrintWriter(new FileWriter(predict_location + "\\" + date + ".txt"));
		
		pw.println("Hot Tag FJB");
		pw.println("   TAG    -  FORUM   -  SCORE   -   PROB   -  MAXPROB  ");
		
		System.out.println("Hot Tag FJB");
		System.out.println("       NO      -      TAG      -     FORUM     -     SCORE     -      PROB     -     MAXPROB    ");
		
		DecimalFormat df = new DecimalFormat("#0.0000000000000");
		
		int i = 0;
		for (Data data : result.data) {
			
			pw.println(++i + " " + data.tag + " " + data.forum + " " + df.format(data.score) +  " " + df.format(data.prob) + " " + df.format(data.max_prob));
			System.out.println(++i + " " + data.tag + " " + data.forum + " " + df.format(data.score) +  " " + df.format(data.prob) + " " + df.format(data.max_prob));
		}
		
		pw.close();
		
		/* export data chart */
		ChartWriter chartTopTen = new ChartWriter(predict_location , result.points , date.toString(), "topTen");
		ChartWriter chartAll    = new ChartWriter(predict_location , points , date.toString(), "all");
		chartTopTen.exportData(database.getTotalDate());
		chartAll.exportData(database.getTotalDate());
		
		/* generate chart */
		Generator topTen = new Generator(true, predict_location + "\\" + date + "topTen.txt");
		topTen.createChart();
		Generator all    = new Generator(false, predict_location + "\\" + date + "all.txt");
		all.createChart();
	}

	public static void main(String[] args) throws Exception {

		inputReader = new BufferedReader(new InputStreamReader(System.in));

		input();

		for (int i = 0; i < days; i++) {
			points = inputProcess();

			if (!init) {
				
				database.markOldestDay();
				database.updateScores();
				printResult();
				database.deleteOldestDay();
				
				System.out.println("Commit? Y/N");
				String commit = inputReader.readLine();

				if (commit.equalsIgnoreCase("Y")) database.commit();
				else database.rollback();
			} else {
				database.commit();
			}
			
			System.out.println(date + ".csv was successfully processed");
			
			/* prepare data for next reading */
			dataReader = new BufferedReader(new FileReader(location + "\\" + date.nextDay() + ".csv"));
			database.close();
		}

		inputReader.close();
	}
}