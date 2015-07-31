import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


public class Separator
{
	String s;
	Set<String> forbidden; //list of forbidden words that already define
	
	public Separator(String location) throws IOException {
		forbidden = new TreeSet<String>();
		BufferedReader inputReader = new BufferedReader(new FileReader(location + "\\forbidden.txt"));
		String temp = inputReader.readLine();
		while(temp != null){
			forbidden.add(temp);
			temp = inputReader.readLine();
		}
		inputReader.close();
	}
	
	public void setString(String s) {
		this.s = s;
	}
	
	/**
	 * Make a list of tag from one row
	 */
	public Set<String> separate() {
		Set<String> result = new TreeSet<String>();
		StringTokenizer token = new StringTokenizer(s," !@#$%^&*()-_=+|\\\'\":;[]{}<>,.?/~`");
		while (token.hasMoreTokens()){
			String temp = token.nextToken().toLowerCase();
			if(!checkNumber(temp)){
				if(!forbidden.contains(temp)){
					result.add(temp);
				}
			}
		}
		return result;
	}
	
	public boolean checkNumber(String a) {
		try {
			Integer.parseInt(a);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
}