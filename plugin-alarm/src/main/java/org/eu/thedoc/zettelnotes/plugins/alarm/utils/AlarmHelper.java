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
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.RecurrenceModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.AlarmReceiver;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.DatabaseService;

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
    RecurrenceModel recurrenceModel = RegexHelper.getInstance().parse(alarmModel);
    if (recurrenceModel == null) {
      return;
    }
    Calendar calendar = alarmModel.getCalendar();
    switch (recurrenceModel.getCOOKIE()) {
      case HOUR -> calendar.add(Calendar.HOUR, recurrenceModel.getDigit());
      case DAY -> calendar.add(Calendar.DATE, recurrenceModel.getDigit());
      case WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, recurrenceModel.getDigit());
      case MONTH -> calendar.add(Calendar.MONTH, recurrenceModel.getDigit());
      case YEAR -> calendar.add(Calendar.YEAR, recurrenceModel.getDigit());
    }
    schedule(alarmModel, calendar);
  }

  public void schedule(AlarmModel model, Calendar calendar) throws SecurityException {
    if (Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() > calendar.getTimeInMillis()) {
      Log.w(getClass().getName(),
          String.format("Time in past %s > %s for %s. %s", Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis(),
              calendar.getTimeInMillis(), model.getId(), model.getText()));
      return;
    }
    Log.v(getClass().getName(), String.format("schedule %s %s. %s", calendar.getTimeInMillis(), model.getId(), model.getText()));

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
