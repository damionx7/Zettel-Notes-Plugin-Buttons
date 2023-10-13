package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.RecurrenceModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.RecurrenceModel.COOKIE;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

public class RegexHelper {

  @Nullable
  public static RecurrenceModel parse(AlarmModel model) {
    String recurrence = model.getRecurrence();
    Matcher matcher = Regex.RECURRENCE.pattern.matcher(recurrence);
    if (matcher.find()) {
      int digit = Integer.parseInt(matcher.group(2));
      final String string = matcher.group(3);
      COOKIE cookie = null;
      String s = string.toLowerCase();
      if (s.equals(COOKIE.HOUR.getConstant())) {
        cookie = COOKIE.HOUR;
      } else if (s.equals(COOKIE.DAY.getConstant())) {
        cookie = COOKIE.DAY;
      } else if (s.equals(COOKIE.WEEK.getConstant())) {
        cookie = COOKIE.WEEK;
      } else if (s.equals(COOKIE.MONTH.getConstant())) {
        cookie = COOKIE.MONTH;
      } else if (s.equals(COOKIE.YEAR.getConstant())) {
        cookie = COOKIE.YEAR;
      }
      if (cookie != null) {
        return new RecurrenceModel(digit, cookie);
      }
    }
    return null;
  }

  public static List<AlarmModel> parse(String category, String fileTitle, String fileUri, String content) {
    List<AlarmModel> list = new ArrayList<>();
    Matcher matcher = Regex.ALARM.pattern.matcher(content);
    while (matcher.find()) {
      AlarmModel model = new AlarmModel();
      model.setCategory(category);
      model.setFileTitle(fileTitle);
      model.setFileUri(fileUri);
      //calendar
      String calendar = matcher.group(1);
      try {
        //Logger.err("ALARM::regex::calendar", calendar);
        model.setCalendar(DateTimeHelper.getCalendar(calendar));
      } catch (Exception e) {
        Log.e(e.getClass().getName(), e.toString());
        continue;
      }
      //recurrence if set
      String recurrence = matcher.group(2);
      if (recurrence != null && !recurrence.isEmpty()) {
        model.setRecurrence(matcher.group(3));
      }
      //text if present
      String text = matcher.group(5);
      if (text != null && !text.isEmpty()) {
        model.setText(text);
        model.setIndexes(new int[]{matcher.start(5), matcher.end(5)});
      } else {
        model.setText(fileTitle);
      }
      list.add(model);
    }
    return list;
  }

  public static boolean matches(String text) {
    Matcher matcher = Regex.ALARM.pattern.matcher(text);
    return matcher.find();
  }
}
