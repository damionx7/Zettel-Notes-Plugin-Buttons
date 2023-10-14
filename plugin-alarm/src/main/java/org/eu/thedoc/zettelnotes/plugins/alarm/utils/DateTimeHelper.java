package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeHelper {

  //https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
  public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm";
  private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);

  public static Calendar getCurrent() {
    return Calendar.getInstance(TimeZone.getDefault());
  }

  public static Calendar getCalendar(final String time, boolean returnDefault) {
    Calendar calendar = getCalendar(time);
    if (calendar == null && returnDefault) {
      return getCurrent();
    }
    return calendar;
  }

  @Nullable
  public static Calendar getCalendar(final String time) {
    Calendar calendar = getCurrent();
    try {
      Date date = mSimpleDateFormat.parse(time);
      if (date != null) {
        calendar.setTime(date);
        return calendar;
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  public static String fromCurrentCalendar() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
    return simpleDateFormat.format(getCurrent().getTime());
  }

  public static String fromCalendar(final Calendar calendar) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
    return simpleDateFormat.format(calendar.getTime());
  }
}
