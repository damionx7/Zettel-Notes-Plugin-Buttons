package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.google.gson.Gson;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel.Recurrence;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.AlarmReceiver;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.DatabaseService;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

//https://github.com/orgzly/orgzly-android/blob/0783e64e122ec4a9595c98c99b975f3e7bbf2870/app/src/main/java/com/orgzly/android/reminders/RemindersScheduler.kt#L48
//https://github.com/orgzly/orgzly-android/blob/20236b4626b1045c68d50d76d1b3bdfcbd90f618/app/src/main/java/com/orgzly/android/reminders/ReminderService.java
public class AlarmHelper {

  private final Context mContext;
  private final AlarmManager mAlarmManager;

  public AlarmHelper(Context context) {
    mContext = context;
    mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  public void schedule(List<AlarmModel> models) throws SecurityException {
    for (AlarmModel model : models) {
      schedule(model, model.getCalendar());
    }
  }

  public void scheduleRecurrence(AlarmModel alarmModel) {
    Calendar calendar = alarmModel.getCalendar();
    String recurrence = alarmModel.getRecurrence();
    Matcher matcher = Regex.RECURRENCE.pattern.matcher(recurrence);
    if (matcher.find()) {
      final String group1 = matcher.group(2);
      final String string = matcher.group(3);
      if (group1 != null && string != null) {
        int digit = Integer.parseInt(group1);
        if (string.equalsIgnoreCase(Recurrence.MIN.getConstant())) {
          calendar.add(Calendar.MINUTE, digit);
        } else if (string.equalsIgnoreCase(Recurrence.HOUR.getConstant())) {
          calendar.add(Calendar.HOUR_OF_DAY, digit);
        } else if (string.equalsIgnoreCase(Recurrence.DAY.getConstant())) {
          calendar.add(Calendar.DAY_OF_YEAR, digit);
        } else if (string.equalsIgnoreCase(Recurrence.WEEK.getConstant())) {
          calendar.add(Calendar.WEEK_OF_YEAR, digit);
        } else if (string.equalsIgnoreCase(Recurrence.MONTH.getConstant())) {
          calendar.add(Calendar.MONTH, digit);
        } else if (string.equalsIgnoreCase(Recurrence.YEAR.getConstant())) {
          calendar.add(Calendar.YEAR, digit);
        }
        schedule(alarmModel, calendar);
      }
    }
  }

  public void schedule(AlarmModel model, Calendar calendar) throws SecurityException {
    if (Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() > calendar.getTimeInMillis()) {
      Log.w(getClass().getName(),
          String.format("Time in past %s > %s for %s. %s", Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis(),
              calendar.getTimeInMillis(), model.getId(), model.getText()));
      return;
    }
    Log.v(getClass().getName(),
        String.format("%s schedule %s %s. %s", calendar.toString(), calendar.getTimeInMillis(), model.getId(), model.getText()));

    PendingIntent pendingIntent = getPendingIntent(model);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    } else {
      mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
  }

  private PendingIntent getPendingIntent(AlarmModel model) {
    Intent intent = new Intent(mContext, AlarmReceiver.class);
    intent.putExtra(DatabaseService.ARGS_CONTENT, new Gson().toJson(model));
    return PendingIntent.getBroadcast(mContext, model.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  @RequiresApi(api = VERSION_CODES.S)
  public boolean canScheduleAlarms() {
    return mAlarmManager.canScheduleExactAlarms();
  }

  public void unschedule(List<AlarmModel> alarmModels) {
    for (AlarmModel model : alarmModels) {
      Log.v(getClass().getName(), String.format("unSchedule id:%s text:%s", model.getId(), model.getText()));
      PendingIntent pendingIntent = getPendingIntent(model);
      mAlarmManager.cancel(pendingIntent);
    }
  }
}
