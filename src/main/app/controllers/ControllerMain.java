package app.controllers;

import app.Config;
import app.models.*;
import app.models.Exporter;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;


public class ControllerMain
{
    private static final int MAXIMUM_CHARACTERS = 15;

    private static Database database;
    private static Visualizator visualizator;
    private static BufferedReader dataReader;
    private static Separator separator;
    private static DateUtility date;
    private static ArrayList<Tag> allVisualization;
    private static HashMap<String, Tag> tag;
    private static String today;
    private static int days;
    private static boolean init;

    public static void runDatabase() throws Exception {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd \"./vendor\" && java -cp hsqldb.jar org.hsqldb.Server -database.0 file:trendFJB -dbname.0 xdb");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = r.readLine();
        while (line != null) {
            System.out.println(line);
            line = r.readLine();
        }
    }

    /**
     * Initialize data history
     * @param date start date
     * @param days window size
     * @return map that will be export to JSON for visualization
     */
    public static Map<String, Object> initialize(String date, int days) {
        Map<String, Object> result = new HashMap<>();
        try {
            database = new Database();
            database.create();
            database.close();
            dataReader = new BufferedReader(new FileReader(Config.inputLocation + "/data/" + date + ".csv"));
            separator = new Separator(Config.inputLocation + "/forbidden.txt", MAXIMUM_CHARACTERS);
            ControllerMain.date = new DateUtility(date);
            ControllerMain.today = date;
            ControllerMain.days = days;
            ControllerMain.init = true;
            process();
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        return result;
    }

    /**
     * Get score for the new day
     *
     * @return map that will be export to JSON for visualization
     */
    public static Map score() {
        Map<String, Object> result = new HashMap<>();
        try {
            database = new Database();
            ControllerMain.date = new DateUtility(database.getNewestDay());
            database.close();
            dataReader = new BufferedReader(new FileReader(Config.inputLocation + "/data/" + date.nextDay() + ".csv"));
            separator = new Separator(Config.inputLocation + "/forbidden.txt", MAXIMUM_CHARACTERS);
            ControllerMain.today = date.toString();
            ControllerMain.days = 1;
            ControllerMain.init = false;
            process();
            result.put("success", true);
            result.put("all", date + "-all");
            result.put("top", date + "-top");
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        return result;
    }

    /* process newly inserted data */
    private static void process() throws Exception {
        for (int i = 0; i < days; i++) {
            tag = new HashMap<>();
            getTags();
            database = new Database(tag, date.toString(), init, MAXIMUM_CHARACTERS);
            database.insert();
            System.out.println("\n" + date + ".csv was successfully inserted to database");
            allVisualization = database.calculate();

            if (!init) {
                database.deleteOldestDay();
                database.deleteUnusedTag();
                exportResult();
            }
            System.out.println(date + ".csv was successfully processed");

			/* prepare data for next reading */
            if (i < days - 1) {
                dataReader = new BufferedReader(new FileReader(Config.inputLocation + "/data/" + date.nextDay() + ".csv"));
            }
            database.close();
        }
    }

    /* Save list of tag */
    private static void getTags() throws Exception {
        String in = dataReader.readLine();

        while (in != null) {
            try {
                String input[] = in.split(",");
                String forum = input[0];
                Integer.parseInt(forum); // check format for every row
                String tags = "";

                for (int i = 1; i < input.length; i++) {
                    tags += input[i];
                }

				/* string (tags) that will be processed */
                separator.setString(tags);

				/* process list of tags from one row */
                Set<String> result = separator.separate();
                for (String s : result) {
                    if (tag.containsKey(s)) {
                        tag.get(s).getData().addCounter();
                    } else {
                        tag.put(s, new Tag(s, forum, new Data(1)));
                    }
                }
            } catch (Exception ignored) {
            }
            in = dataReader.readLine();
        }
    }

    /**
     * Export Trending Tag FJB for summary table and visualization
     */
    private static void exportResult() throws Exception {
        visualizator = new Visualizator(ControllerMain.today, MAXIMUM_CHARACTERS);
        visualizator.retrieveTrending();
        Map<String, Object> top = visualizator.exportData(visualizator.getTopVisualization(), "datasets");
        Map<String, Object> all = visualizator.exportData(allVisualization, "datasets");

		/* export for visualization */
        Exporter chartTop = new Exporter(top, Config.resultLocation, date.toString(), "top");
        chartTop.close();
        Exporter chartAll = new Exporter(all, Config.resultLocation, date.toString(), "all");
        chartAll.close();

        /* export for summary */
        Exporter summary = new Exporter(visualizator.getTopSummary(), Config.summaryLocation + "/" + today);
        summary.close();
    }

    /* delete all the result file */
    public static Map<String, Object> deleteResult() {
        Map<String, Object> result = new HashMap<>();
        try {
            File directory = new File(Config.resultLocation);
            FileUtils.cleanDirectory(directory);
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        return result;
    }
}