package app.models;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtility
{
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
    private static final long DAY_MILISECOND = 1000 * 60 * 60 * 24;

    private Date date;

    public DateUtility(String date, String format) {
        try {
            this.date = new SimpleDateFormat(format).parse(date);
        } catch (Exception ex) {
            this.date = new Date();
        }
    }

    public DateUtility(String date) {
        try {
            this.date = new SimpleDateFormat(DEFAULT_FORMAT).parse(date);
        } catch (Exception ex) {
            this.date = new Date();
        }
    }

    public DateUtility(Date date) {
        this.date = date;
    }

    public DateUtility() {
        this.date = new Date();
    }

    public DateUtility prevDay(int k) {
        long day = DAY_MILISECOND * k;
        date.setTime(this.date.getTime() - day);
        return this;
    }

    public DateUtility nextDay(int k) {
        long day = DAY_MILISECOND * k;
        date.setTime(this.date.getTime() + day);
        return this;
    }

    public DateUtility prevDay() {
        long day = DAY_MILISECOND;
        date.setTime(this.date.getTime() - day);
        return this;
    }

    public DateUtility nextDay() {
        long day = DAY_MILISECOND;
        date.setTime(this.date.getTime() + day);
        return this;
    }

    public String toString(String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public String toString() {
        return new SimpleDateFormat(DEFAULT_FORMAT).format(date);
    }
}
