package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

public class RegexHelper {

  private RegexHelper() {}

  public static boolean matches(String text) {
    Matcher matcher = Regex.ALARM.pattern.matcher(text);
    return matcher.find();
  }

  public static List<AlarmModel> parse(String category, String fileTitle, String fileUri, String content) {
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
          Log.e("RegexHelper", String.format("Parsed Calendar Null Date Format %s", calendar));
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
        //check if task
        checkIfTask(model, text);
        //model.setText(text);
        //model.setIndexes(new int[]{matcher.start(5), matcher.end(5)});
      }
      model.setText(matcher.group());
      model.setIndexes(new int[]{matcher.start(), matcher.end()});

      models.add(model);
    }
    return models;
  }

  private static void checkIfTask(AlarmModel model, String text) {
    Matcher matcher = Regex.TASK.pattern.matcher(text);
    if (matcher.find()) {
      model.setType(AlarmModel.TYPE_TASK);
      String checkedString = matcher.group(2);
      if (checkedString != null) {
        model.setChecked(checkedString.equalsIgnoreCase("x"));
      }
    }
  }
}
