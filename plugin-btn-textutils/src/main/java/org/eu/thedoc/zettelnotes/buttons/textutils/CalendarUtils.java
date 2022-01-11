package org.eu.thedoc.zettelnotes.buttons.textutils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarUtils {

  public static String getYearTable (int year, boolean addEmptyRows) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\n");
    Calendar c = Calendar.getInstance();
    c.set(Calendar.YEAR, year);
    for (int i = 0; i < 12; i++) {
      c.set(Calendar.MONTH, i);
      SimpleDateFormat format = new SimpleDateFormat("MMM", Locale.getDefault());
      stringBuilder.append(format.format(c.getTime())).append("\n");
      stringBuilder.append(CalendarUtils.getMonthTable(c.get(Calendar.YEAR), c.get(Calendar.MONTH), addEmptyRows));
      stringBuilder.append("\n\n");
    }
    return stringBuilder.toString();
  }

  public static String getMonthTable (int year, int month, boolean addEmptyRow) {
    StringBuilder monthTable = new StringBuilder();

    //set calendar
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, 1);

    int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

    //first row
    int daysInWeek = 7;
    monthTable.append("\n").append("|");
    for (int i = 0; i < daysInWeek; i++) {
      calendar.set(Calendar.DAY_OF_WEEK, i + 1);
      SimpleDateFormat format = new SimpleDateFormat("EEE", Locale.getDefault());
      addCell(monthTable, format.format(calendar.getTime()));
    }

    //second dashes row
    monthTable.append("\n").append("|");
    addRepeatingRow(monthTable, daysInWeek, "--");
    //days row
    monthTable.append("\n").append("|");
    int day = 1;
    //days of calendar start from zero
    for (int offset = 1; offset < firstDayOfWeek; offset++) {
      addCell(monthTable, "  ");
    }
    firstDayOfWeek = firstDayOfWeek - 1;
    while (day <= daysInMonth) {
      addCell(monthTable, String.valueOf(day));
      //new line for new week
      if ((firstDayOfWeek + day) % 7 == 0) {
        //check if last day
        if (day != daysInMonth) {
          monthTable.append("\n").append("|");
          if (addEmptyRow) {
            addRepeatingRow(monthTable, daysInWeek, "  ");
            monthTable.append("\n").append("|");
          }
        }
      }
      //last day
      //append empty row if needed
      if (day == daysInMonth) {
        //fill empty columns
        int offset = 7 - ((firstDayOfWeek + day) % 7);
        Log.w("offset", "" + offset);
        if (offset < 7) {
          addRepeatingRow(monthTable, offset, "  ");
        }
        if (addEmptyRow) {
          monthTable.append("\n").append("|");
          addRepeatingRow(monthTable, daysInWeek, "  ");
        }
      }
      day++;
    }
    return monthTable.toString();
  }

  private static void addCell (StringBuilder stringBuilder, String value) {
    stringBuilder.append("  ").append(value).append("  ").append("|");
  }

  private static void addRepeatingRow (StringBuilder stringBuilder, int cells, String value) {
    for (int i = 1; i <= cells; i++) {
      addCell(stringBuilder, value);
    }
  }

}
