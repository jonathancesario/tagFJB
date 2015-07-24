import java.util.Calendar;


public class TheDate
{
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
}
