package app.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


/**
 * Make a list of tag from raw data that will be processed
 */
public class Separator
{
    private Set<String> forbidden; //list of forbidden words that already defined
    private String s;
    private int maxChar;

    public Separator(String location, int maxChar) throws IOException {
        forbidden = new TreeSet<>();
        BufferedReader inputReader = new BufferedReader(new FileReader(location));
        String temp = inputReader.readLine();
        while(temp != null){
            forbidden.add(temp);
            temp = inputReader.readLine();
        }
        inputReader.close();
        this.maxChar = maxChar;
    }

    public void setString(String s) {
        this.s = s;
    }

    /**
     * Make a list of tag from one row
     */
    public Set<String> separate() {
        Set<String> result = new TreeSet<>();
        StringTokenizer token = new StringTokenizer(s," !@#$%^&*()-_=+|\\\'\":;[]{}<>,.?/~`");
        while (token.hasMoreTokens()){
            String temp = token.nextToken().toLowerCase();
            if(temp.length() <= maxChar && !isNumber(temp.substring(0,5)) && !forbidden.contains(temp)) {
                result.add(temp);
            }
        }
        return result;
    }

    /** true if number */
    private boolean isNumber(String a) {
        try {
            Integer.parseInt(a);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
}