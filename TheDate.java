import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class TheDate
{
	String utc;
	long unix;
	
	public String getToday() {
		Calendar calendar = Calendar.getInstance();
		String year = ""+calendar.get(Calendar.YEAR);
		String month = ""+calendar.get(Calendar.MONTH);
		if(calendar.get(Calendar.MONTH)+1 < 10){
			int x = calendar.get(Calendar.MONTH)+1;
			month = "0"+x;
		}
		String day = ""+calendar.get(Calendar.DAY_OF_MONTH);
		if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
			day = "0"+day;
		}
		return year+"-"+month+"-"+day;
	}
	
	public String toUTC(long inputDate) {
		Date date = new Date(inputDate*1000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
		utc = sdf.format(date);
		return utc;
	}
	
	public long toUnix(String inputDate) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm:ss z");
		Date date = dateFormat.parse(inputDate);
		unix = (long) date.getTime()/1000;
		return unix;
	}
}
