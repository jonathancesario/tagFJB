package app.models;

import app.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class Database
{
    private static final String HOST = Config.DBHOST;
    private static final String USER = Config.DBUSER;
    private static final String PASSWORD = Config.DBPASSWORD;
    private static final String PACKAGE = Config.DBPACKAGE;

    private Connection conn;
    private Statement stat;
    private Map<String, Tag> todayTag;
    private String[] newTag;
    private String today, oldestDay;
    private boolean init;
    private int maxChar;

    public Database() throws Exception {
        Class.forName(PACKAGE);
        this.conn = DriverManager.getConnection(HOST, USER, PASSWORD);
        this.stat = conn.createStatement();
    }

    /**
     * Constructor for calculate probability, score
     * @param tags list of tag
     * @param init true if init, false if score
     */
    public Database(HashMap<String, Tag> tags, String today, boolean init, int maxChar) throws Exception {
        Class.forName(PACKAGE);
        this.conn = DriverManager.getConnection(HOST, USER, PASSWORD);
        this.stat = conn.createStatement();
        this.init = init;
        this.today = today;
        this.maxChar = maxChar;
        this.todayTag = new TreeMap<>();

        for (Map.Entry<String, Tag> e : tags.entrySet()) {
            this.todayTag.put(e.getKey(), e.getValue());
        }
    }

    /* initialize database tables */
    public void create() throws Exception {
        stat.executeQuery("CREATE TABLE IF NOT EXISTS RATING( "
                + "tag varchar(25) primary key, "
                + "forum char(3) not null, "
                + "score double)");

        stat.executeQuery("CREATE TABLE IF NOT EXISTS HISTORY( "
                        + "tag varchar(25) not null, "
                        + "date date not null, "
                        + "counter int, "
                        + "probability double, "
                        + "primary key(tag,date), "
                        + "foreign key(tag) references rating(tag) ON UPDATE CASCADE ON DELETE CASCADE)"
        );
        stat.executeQuery("DELETE FROM RATING");
    }

    public int getCountTag(String date) throws Exception {
        ResultSet temp = stat.executeQuery("SELECT count(distinct tag) as counter FROM HISTORY where date = '" + date + "'");
        return temp.next() ? temp.getInt("counter") : 0;
    }

    public int getSumCounter() throws Exception {
        ResultSet temp = stat.executeQuery("SELECT SUM(counter) as total FROM HISTORY WHERE date = '" + today + "'");
        return temp.next() ? temp.getInt("total") : 0;
    }

    public int getCounter(String key) throws Exception {
        ResultSet temp = stat.executeQuery("SELECT counter FROM HISTORY where tag = '" + key + "'" + " and date = '" + today + "'");
        return temp.next() ? temp.getInt("counter") : 0;
    }

    public double getMaxProb(String key) throws Exception {
        ResultSet temp = stat.executeQuery("SELECT MAX(probability) as maxProb FROM HISTORY " +
                "where tag = '" + key + "' and date < '" + today + "'");
        return temp.next() ? temp.getDouble("maxProb") : 0;
    }

    public double getScore(String key) throws Exception {
        ResultSet temp = stat.executeQuery("SELECT score FROM RATING where tag = '" + key + "'");
        return temp.next() ? temp.getDouble("score") : 0;
    }

    public String getNewestDay() throws Exception {
        ResultSet temp = stat.executeQuery("SELECT MAX(date) as maxDate FROM HISTORY");
        return temp.next() ? temp.getString("maxDate") : "";
    }

    public String getOldestDay() throws Exception {
        ResultSet temp = stat.executeQuery("SELECT MIN(date) as minDate FROM HISTORY");
        if (temp.next()) {
            oldestDay = temp.getString("minDate");
        }
        return oldestDay;
    }

    public void deleteOldestDay() throws Exception {
        if (oldestDay != null) {
            stat.execute("DELETE FROM HISTORY WHERE date = '" + oldestDay + "'");
        }
    }

    /* delete tag that doesn't exist anymore on HISTORY */
    public void deleteUnusedTag() throws Exception {
        stat.executeQuery("DELETE FROM RATING where tag in " +
                "(SELECT tag from rating WHERE not exists" +
                "(select distinct tag FROM HISTORY WHERE rating.tag = history.tag))");
    }

    public void close() throws Exception {
        stat.close();
        conn.close();
    }

    /* change oldTag's name to newTag */
    public void updateTag(String oldTag, String newTag) throws Exception {
        stat.executeQuery("UPDATE RATING SET tag = '" + newTag + "' WHERE tag = '" + oldTag + "'");
    }

    public void insertTag(String key, int counter) throws Exception {
        stat.executeQuery("INSERT INTO RATING VALUES('" + key + "','" + counter + "',0)");
    }

    /**
     * Insert new data to HISTORY
     */
    public void insert() throws Exception {
        Map<String, Integer> oldTags = new TreeMap();

        /* get all tag */
        ResultSet rs = stat.executeQuery("SELECT tag FROM RATING");
        while (rs.next()) {
            String name = rs.getString("tag");
            oldTags.put(name, getCounter(name));
        }

        /* similar tag grouping based on Levenshtein distance */
        Grouper grouper = new Grouper(todayTag, oldTags, maxChar);
        Map<String, Integer> newTags = grouper.groupSimilarTag();

        /* add all new tags to HISTORY */
        for (Map.Entry<String, Integer> entry : newTags.entrySet()) {
            stat.execute("INSERT INTO HISTORY VALUES('" + entry.getKey() + "','" + today + "'," + entry.getValue() + ",0)");
        }

        /* migrate to newTag */
        this.newTag = newTags.keySet().toArray(new String[newTags.size()]);
    }

    /**
     * Calculate probability, score
     */
    public ArrayList<Tag> calculate() throws Exception {
        ArrayList<Tag> result = new ArrayList<>(); // coordinate for visualization
        Visualizator visualizator = new Visualizator();

        int total = getSumCounter();
        int tag_count = getCountTag(today);

        getOldestDay();

        if (!init) {
            stat.executeQuery("UPDATE RATING SET score = score/2.0"); // decay the score exponentially
        }

        for (String key : newTag) {
            /* update probability */
            int counter = getCounter(key);
            double prob = ((double) counter + 1) / ((double) total + tag_count);
            stat.execute("UPDATE HISTORY SET probability = " + prob + " WHERE date = '" + today + "' and tag = '" + key + "'");

			/* add every tags for visualization */
            result = visualizator.getHistory(result, key, this.oldestDay);

			/* update KL divergence with smoothing */
            if (!init) {
                /* maximum probability */
                double maxProb = getMaxProb(key);

                /* if there is no tag like this before */
                if (maxProb < 1e-9) {
                    maxProb = 1 / (double) (tag_count + total);
                }

				/* update score */
                double score = getScore(key);
                score += prob * Math.log(prob / maxProb);
                stat.execute("UPDATE RATING SET score = " + score + " WHERE tag = '" + key + "'");
            }
        }
        return result;
    }
}
