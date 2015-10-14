package app.models;


public class Data
{
    private String date;
    private int counter;
    private double prob;
    private double score;
    private double maxProb;

    /* Constructor for trending summary  */
    public Data(double prob, double score, double maxProb) {
        this.prob = prob;
        this.score = score;
        this.maxProb = maxProb;
    }

    /* Constructor for separator */
    public Data(String date, int counter) {
        this.date = date;
        this.counter = counter;
    }

    public Data(int counter) {
        this.counter = counter;
    }

    public String getDate() { return date; }

    public int getCounter() { return counter; }

    public double getProb() { return prob; }

    public double getScore() { return score; }

    public double getMaxProb() { return maxProb; }

    public void addCounter() { counter++; }
}