package app.models;

import java.util.ArrayList;


public class Tag
{
    private String name, forum;
    private Data dataPresent;
    private ArrayList<Data> dataHistory;

    public Tag (String name, String forum, ArrayList<Data> dataHistory) {
        this.name = name;
        this.forum = forum;
        this.dataHistory = dataHistory;
    }

    public Tag (String name, String forum, Data dataPresent) {
        this.name = name;
        this.forum = forum;
        this.dataPresent = dataPresent;
    }

    public Tag (String name, ArrayList<Data> dataHistory) {
        this.name = name;
        this.dataHistory = dataHistory;
    }

    public void addData(String date, int counter) {
        dataHistory.add(new Data(date,counter));
    }

    public Data getData() {
        return dataPresent;
    }

    public Data getData(int index) {
        return dataHistory.get(index);
    }

    public String getName() {
        return name;
    }

    public String getForum() {
        return forum;
    }

    public int getDataSize() {
        return dataHistory.size();
    }
}
