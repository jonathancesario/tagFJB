package app.controllers;

import app.Config;
import app.models.DateUtility;
import app.models.Tag;
import app.models.Visualizator;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class ControllerUI
{
    private static final Gson gson = new Gson();
    private static Visualizator visualizator;
    private static BufferedReader dataReader;

    public static Set<String> getResultList() throws Exception {
        File folder = new File(Config.resultLocation);
        File[] listOfFiles = folder.listFiles();
        Set<String> temp = new TreeSet<>();
        for (File file : listOfFiles)
            if (file.isFile()) {
                String[] splitter = file.getName().split("\\.");
                if (splitter.length == 2 && splitter[1].equals("JSON")) {
                    temp.add(splitter[0]);
                }
            }
        return temp;
    }

    public static Map<String, Object> getResultByTag(String tag) throws Exception {
        Map<String, Object> result = new HashMap<>();
        visualizator = new Visualizator();
        try {
            ArrayList tags = new ArrayList<>();
            tags.add(tag);
            tags = visualizator.getCounterHistory(tags);

            Map<String, Object> data = visualizator.exportData(tags, "datasets_win");
            data.put("datasets_lose", "");

            result.put("success", true);
            result.put("data", data);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        visualizator.close();
        return result;
    }

    /* Get JSON for visualization of one day */
    public static Map<String, Object> getResult(String name) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>(); // will be put to result
            Map<String, Object> dataLose = new HashMap<>(); // lose tag
            Map<String, Object> dataWin = exportJSON(Config.resultLocation + "/" + name + ".JSON"); // today's trending tag

            /* change key name of today's trending tag */
            Object datasets = (Object) dataWin.get("datasets");
            data.put("datasets_win", datasets);

            /* put date */
            datasets = (Object) dataWin.get("labels");
            data.put("labels", datasets);

            /* initialization  */
            DateUtility d = new DateUtility(name.substring(0, 10));
            visualizator = new Visualizator();

            /* save list of today's trending tag for comparison */
            datasets = (Object) dataWin.get("datasets");
            DataJSON[] todayTop = gson.fromJson(gson.toJson(datasets), DataJSON[].class);
            Set<String> todayTopTag = new HashSet<>();
            for (DataJSON aTodayTop : todayTop) {
                todayTopTag.add(aTodayTop.label);
            }

            /* process every trending tag except today */
            boolean done = false;
            while (!done) {
                d.prevDay();
                String location = Config.resultLocation + "/" + d.toString() + "-top.JSON";

                File f = new File(location);
                if (f.exists() && !f.isDirectory()) {
                    /* save list of d's trending tag */
                    Map<String, Object> temp = exportJSON(location);
                    datasets = (Object) temp.get("datasets");
                    DataJSON[] historyTop = gson.fromJson(gson.toJson(datasets), DataJSON[].class);

                    /* get list of lose tag */
                    ArrayList<String> historyTopTag = new ArrayList<>();
                    for (DataJSON aHistoryTop : historyTop) {
                        if (!todayTopTag.contains(aHistoryTop.label)) {
                            historyTopTag.add(aHistoryTop.label);
                        }
                    }

                    ArrayList<Tag> t = visualizator.getCounterHistory(historyTopTag);
                    dataLose.putAll(visualizator.exportData(t, "datasets_lose"));
                } else {
                    done = true;
                }
            }
            if (dataLose.get("datasets_lose") == null) {
                data.put("datasets_lose", "");
            } else {
                data.put("datasets_lose", dataLose.get("datasets_lose"));
            }
            result.put("data", data);
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        visualizator.close();
        return result;
    }

    /* get detailed properties of trending tags (score, prob, maxProb) */
    public static Map<String,Object> getSummary(String date) throws Exception {
        Map<String, Object> result = new HashMap<>();
        try {
            dataReader = new BufferedReader(new FileReader(Config.summaryLocation + "/" + date + ".JSON"));
            String tmp = dataReader.readLine();
            StringBuffer buff = new StringBuffer();
            while (tmp != null) {
                buff.append(tmp);
                tmp = dataReader.readLine();
            }
            result.put("data", buff.toString());
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        dataReader.close();
        return result;
    }

    /* Make a map from JSON */
    private static Map<String, Object> exportJSON(String location) throws Exception {
        dataReader = new BufferedReader(new FileReader(location));
        String tmp = dataReader.readLine();
        StringBuffer buff = new StringBuffer();
        while (tmp != null) {
            buff.append(tmp);
            tmp = dataReader.readLine();
        }

        Gson gson = new Gson();
        dataReader.close();
        return gson.fromJson(buff.toString(), Map.class);
    }
}

class DataJSON {
    int[] data;
    String label;
}

