package app.models;

import app.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class Visualizator
{
    private static final String HOST = Config.DBHOST;
    private static final String USER = Config.DBUSER;
    private static final String PASSWORD = Config.DBPASSWORD;
    private static final String PACKAGE = Config.DBPACKAGE;
    private static final int MINIMUM_COUNTER = 30;
    private static final int TOP_RANK = 5;

    private Connection conn;
    private Statement stat;
    private ArrayList<Tag> topVisualization;
    private ArrayList<Tag> topSummary;
    private String today;
    private int maxChar;

    public Visualizator() throws Exception {
        Class.forName(PACKAGE);
        this.conn = DriverManager.getConnection(HOST, USER, PASSWORD);
        this.stat = conn.createStatement();
    }

    public Visualizator(String today, int maxChar) throws Exception {
        Class.forName(PACKAGE);
        this.conn = DriverManager.getConnection(HOST, USER, PASSWORD);
        this.stat = conn.createStatement();
        this.today = today;
        this.maxChar = maxChar;
    }

    public void close() throws Exception {
        stat.close();
        conn.close();
    }

    public String getNewestDay() throws Exception {
        ResultSet temp = stat.executeQuery("SELECT MAX(date) as maxDate FROM HISTORY");
        return temp.next() ? temp.getString("maxDate") : "";
    }

    public int getCountDate() throws Exception {
        ResultSet temp = stat.executeQuery("SELECT COUNT(DISTINCT date) as total FROM HISTORY");
        return temp.next() ? temp.getInt("total") : 0;
    }

    public double getMaxProb(String key) throws Exception {
        ResultSet temp = stat.executeQuery("SELECT MAX(probability) as maxProb FROM HISTORY " +
                "where tag = '" + key + "' and date < '" + today + "'");
        return temp.next() ? temp.getDouble("maxProb") : 0;
    }

    public ArrayList<Tag> getTopVisualization() {
        return topVisualization;
    }

    public ArrayList<Tag> getTopSummary() {
        return topSummary;
    }

    public void retrieveTrending() throws Exception {
        topVisualization = new ArrayList<>();
        topSummary = new ArrayList<>();

        ResultSet rs = stat.executeQuery("SELECT tag,forum,probability,score,counter FROM RATING r, HISTORY h"
                + " WHERE h.tag = r.tag and date = '" + today + "' and counter > " + MINIMUM_COUNTER + " ORDER BY score desc limit " + TOP_RANK);
        while (rs.next()) {
            String tag = rs.getString("tag");
            String forum = rs.getString("forum");
            double prob = rs.getDouble("probability");
            double score = rs.getDouble("score");

			/* add history data for visualization */
            ResultSet in = stat.executeQuery("SELECT date, counter FROM HISTORY WHERE tag = '" + tag + "'");
            ArrayList<Data> h = new ArrayList<>();
            while (in.next()) {
                h.add(new Data(in.getString("date"), in.getInt("counter")));
            }
            topVisualization.add(new Tag(tag, h));

            double maxProb = getMaxProb(tag); // comparison probability

            topSummary.add(new Tag(tag, forum, new Data(prob, score, maxProb)));
        }
    }

    public ArrayList<Tag> getHistory(ArrayList<Tag> temp, String tag, String oldestDay) throws Exception {
        ArrayList<Tag> result = temp;
        ResultSet rs = stat.executeQuery("SELECT date, counter, probability FROM HISTORY " +
                "where tag = '" + tag + "' and date > '" + oldestDay + "'");
        ArrayList<Data> history = new ArrayList<>(); // history for chart
        while (rs.next()) {
            String date = rs.getString("date");
            int counterTag = rs.getInt("counter");
            history.add(new Data(date, counterTag));
        }
        result.add(new Tag(tag, history)); // add all tags to chart
        return result;
    }

    public ArrayList<Tag> getCounterHistory(ArrayList<String> history) throws Exception {
        ArrayList<Tag> result = new ArrayList<>();

        if (history.isEmpty()) {
            return result;
        }

        String query = "SELECT date,tag,counter FROM HISTORY " +
                "WHERE tag = '" + history.get(0) + "'";
        for (int i = 1; i < history.size(); i++) {
            query += " or tag = '" + history.get(i) + "'";
        }

        ResultSet temp = stat.executeQuery(query);
        HashMap<String,Tag> map = new HashMap<>();
        while (temp.next()) {
            String tag = temp.getString("tag");
            String date = temp.getString("date");
            int counter = temp.getInt("counter");

            if (!map.containsKey(tag)) {
                map.put(tag, new Tag(tag, new ArrayList<>()));
            }

            map.get(tag).addData(date,counter);
        }

        Set<String> theKey = map.keySet();
        for (String key : theKey) {
            result.add(map.get(key));
        }
        return result;
    }

    /* Convert data to Map */
    public Map<String, Object> exportData(ArrayList<Tag> tag, String name) throws Exception {
        Map<String, Object> result = new HashMap<>();
        int size = getCountDate();
        String[] date = new String[size];
        DateUtility dateUtility = new DateUtility(getNewestDay());

        /* get available days */
        for (int i = size - 1; i >= 0; i--) {
            date[i] = dateUtility.toString();
            dateUtility.prevDay();
        }

        ArrayList<Object> dataList = getTagFrequencies(tag, date);

        Collections.sort(dataList, new CounterBasedComparator());

        result.put(name, dataList);
        result.put("labels", date);

        return result;
    }

    /* get all lines and add it all on the dataList */
    private ArrayList<Object> getTagFrequencies(ArrayList<Tag> tag, String[] date) {
        ArrayList<Object> dataList = new ArrayList<>();
        for (Tag aTag : tag) {
            Map<String, Object> tmp = new HashMap<>();
            ArrayList list = getTagFrequency(aTag, date);

            /* JSON format */
            tmp.put("label", aTag.getName());
            tmp.put("data", list);

            dataList.add(tmp);
        }
        return dataList;
    }

    private ArrayList<Object> getTagFrequency(Tag t, String[] date) {
        ArrayList list = new ArrayList();
        int counter = 0;

        for (int j = 0; j < date.length; j++) {
            if (counter < t.getDataSize() && t.getData(counter).getDate().equals(date[j])) {
                list.add(t.getData(counter).getCounter());
                counter++;
            } else {
                list.add(0);
            }
        }

        return list;
    }
}

class CounterBasedComparator implements Comparator<Object> {
    @Override
    public int compare(Object a, Object b) {
        Map<String, Object> ta = (Map<String, Object>) a;
        Map<String, Object> tb = (Map<String, Object>) b;

        ArrayList<Integer> aa = (ArrayList<Integer>) ta.get("data");
        ArrayList<Integer> bb = (ArrayList<Integer>) tb.get("data");

        int suma = aa.get(aa.size() - 1);
        int sumb = bb.get(bb.size() - 1);

        return sumb - suma;
    }
}