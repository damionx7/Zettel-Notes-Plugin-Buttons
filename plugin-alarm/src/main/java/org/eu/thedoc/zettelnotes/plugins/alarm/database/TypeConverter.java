package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import com.google.gson.Gson;
import java.util.Calendar;
import java.util.TimeZone;

public class TypeConverter {

  @androidx.room.TypeConverter
  public static Calendar toCalendar(long ms) {
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    calendar.setTimeInMillis(ms);
    return calendar;
  }

  @androidx.room.TypeConverter
  public static long fromCalendar(Calendar calendar) {
    if (calendar == null) {
      return 0;
    }
    return calendar.getTimeInMillis();
  }

  @androidx.room.TypeConverter
  public static int[] toIntArray(String json) {
    return new Gson().fromJson(json, int[].class);
  }

  @androidx.room.TypeConverter
  public static String fromIntArray(int[] array) {
    return new Gson().toJson(array);
  }

}
