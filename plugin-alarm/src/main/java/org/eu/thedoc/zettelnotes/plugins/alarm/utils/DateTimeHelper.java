package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeHelper {

  //https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
  public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm";
  private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);

  public static Calendar getCalendar(final String time) throws ParseException {
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    calendar.setTime(mSimpleDateFormat.parse(time));
    return calendar;
  }

  public static String fromCurrentCalendar() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
    return simpleDateFormat.format(Calendar.getInstance().getTime());
  }

  public static String fromCalendar(final Calendar calendar) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
    return simpleDateFormat.format(calendar.getTime());
  }
}
