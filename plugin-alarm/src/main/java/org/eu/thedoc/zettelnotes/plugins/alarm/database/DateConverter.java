package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import androidx.room.TypeConverter;
import java.util.Calendar;

public class DateConverter {

  @TypeConverter
  public static Calendar toCalendar(long ms) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(ms);
    return calendar;
  }

  @TypeConverter
  public long fromCalendar(Calendar calendar) {
    return calendar.getTimeInMillis();
  }

}
