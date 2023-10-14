package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.RecurrenceModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.RecurrenceModel.COOKIE;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

public class RegexHelper {

  private static RegexHelper sINSTANCE;

  private RegexHelper() {}

  public static RegexHelper getInstance() {
    if (sINSTANCE == null) {
      sINSTANCE = new RegexHelper();
    }
    return sINSTANCE;
  }

  public boolean matches(String text) {
    Matcher matcher = Regex.ALARM.pattern.matcher(text);
    return matcher.find();
  }

  @Nullable
  public RecurrenceModel parse(AlarmModel model) {
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

  public List<AlarmModel> parse(String category, String fileTitle, String fileUri, String content) {
    List<AlarmModel> models = new ArrayList<>();
    Matcher matcher = Regex.ALARM.pattern.matcher(content);
    while (matcher.find()) {
      AlarmModel model = new AlarmModel();
      model.setType(AlarmModel.TYPE_NOTE);
      model.setCategory(category);
      model.setFileTitle(fileTitle);
      model.setFileUri(fileUri);
      //calendar
      String calendar = matcher.group(1);
      try {
        //Logger.err("ALARM::regex::calendar", calendar);
        Calendar parsedCalendar = DateTimeHelper.getCalendar(calendar);
        if (parsedCalendar != null) {
          model.setCalendar(parsedCalendar);
        } else {
          Log.e(getClass().getName(), String.format("Parsed Calendar Null Date Format %s", calendar));
          continue;
        }
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
        //check if task
        checkIfTask(model, text);
      } else {
        model.setText(fileTitle);
      }
      models.add(model);
    }
    return models;
  }

  private void checkIfTask(AlarmModel model, String text) {
    Matcher matcher = Regex.TASK.pattern.matcher(text);
    if (matcher.find()) {
      model.setType(AlarmModel.TYPE_TASK);
      String checkedString = matcher.group(2);
      if (checkedString != null) {
        model.setChecked(checkedString.equals("x"));
      }
    }
  }
}
