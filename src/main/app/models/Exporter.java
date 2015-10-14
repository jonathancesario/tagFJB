package app.models;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;


public class Exporter
{
    private Gson gson;
    private PrintWriter pw;

    /* visualization */
    public Exporter(Map<String, Object> map, String location, String today, String suffix) throws Exception {
        gson = new Gson();
        pw = new PrintWriter(new FileWriter(location + "/" + today + "-" + suffix + ".JSON"));
        pw.println(gson.toJson(map));
    }

    /* summary */
    public Exporter(ArrayList<Tag> data, String location) throws Exception {
        gson = new Gson();
        pw = new PrintWriter(new FileWriter(location + "-top.JSON"));
        pw.println(gson.toJson(data));
    }

    public void close() { pw.close(); }
}
