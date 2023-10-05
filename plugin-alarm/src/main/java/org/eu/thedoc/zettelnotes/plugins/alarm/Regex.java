package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;

public class Regex {

  // [scheduled]: <2023-09-01 09:12 .+1w>
  private static final Pattern PATTERN = Pattern.compile(
      "\\[scheduled\\]: <(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})(| [ .](\\+\\d+?\\w))>(\\n|)(^.*$|)", Pattern.MULTILINE);

  //https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
  private static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm";

  private final Calendar mCalendar;
  private final SimpleDateFormat mSimpleDateFormat;

  private Regex() {
    mCalendar = Calendar.getInstance();
    mSimpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH);
  }

  public static Regex getInstance() {
    return new Regex();
  }

  public List<AlarmModel> getModels(String category, String file, String content) {
    List<AlarmModel> list = new ArrayList<>();
    Matcher matcher = PATTERN.matcher(content);
    while (matcher.find()) {
      AlarmModel model = new AlarmModel();
      model.setCategory(category);
      model.setFile(file);
      //calendar

      String calendar = matcher.group(1);
      try {
        mCalendar.setTime(mSimpleDateFormat.parse(calendar));
      } catch (NullPointerException | ParseException e) {
        Log.e("regex", e.toString());
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
      }
      list.add(model);
    }
    return list;
  }
}
