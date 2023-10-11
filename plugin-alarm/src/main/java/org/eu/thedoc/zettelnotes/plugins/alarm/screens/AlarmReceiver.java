package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;
import org.eu.thedoc.zettelnotes.broadcasts.AbstractPluginReceiver;
import org.eu.thedoc.zettelnotes.plugins.alarm.BuildConfig;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.AlarmUtils;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.NotificationHelper;

public class AlarmReceiver
    extends BroadcastReceiver {

  NotificationHelper mNotificationHelper;
  AlarmUtils mAlarmUtils;

  private String getNotificationChannelName() {
    return "Reminder";
  }

  private String getNotificationTitle() {
    return "Reminder!";
  }

  private String getNotificationChannelID() {
    return "channel_notify";
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    mNotificationHelper = new NotificationHelper(context);
    mAlarmUtils = new AlarmUtils(context);

    String json = intent.getStringExtra(DatabaseService.ARGS_MODEL);
    if (json != null && !json.isEmpty()) {
      //show notification
      AlarmModel model = new Gson().fromJson(json, AlarmModel.class);
      NotificationCompat.Builder notification = mNotificationHelper.buildNotification(getNotificationChannelID(), model.getFileTitle(),
          model.getText(), "", getNotificationTitle(), R.drawable.ic_notification_alarm, false);
      notification.setPriority(NotificationCompat.PRIORITY_HIGH);
      notification.setAutoCancel(true);

      AbstractPluginReceiver.IntentBuilder intentBuilder = AbstractPluginReceiver.IntentBuilder.getInstance().setActionOpenUri().setUri(
          model.getFileUri()).setLineIndexes(model.getIndexes()).setEdit(true).setRepository(model.getCategory());
      if (BuildConfig.DEBUG) {
        intentBuilder.setDebug();
      }
      Intent pendingIntent = intentBuilder.build();

      PendingIntent pendingViewerIntent = PendingIntent.getActivity(context, model.getId(), pendingIntent,
          PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
      notification.setContentIntent(pendingViewerIntent);

      //show tick task notification button
      //String replacement = null;
      //if (model.getText().startsWith("- [ ]")) {
      //  replacement = "- [x]";
      //} else if (model.getText().startsWith("- [x]")) {
      //  replacement = "- [ ]";
      //}
      //if (replacement != null) {
      //  intentBuilder.setActionOpenAndReplace(replacement + model.getText().substring(5));
      //  Intent tickTask = intentBuilder.build();
      //  PendingIntent tickPendingIntent = PendingIntent.getActivity(context, model.getId(), tickTask,
      //      PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
      //  notification.addAction(new Action(R.drawable.ic_notification_check, "Toggle CheckBox", tickPendingIntent));
      //}

      mNotificationHelper.showNotification(getNotificationChannelID(), getNotificationChannelName(), model.getId(), notification.build());

      //schedule recurrence
      if (model.getRecurrence() != null && !model.getRecurrence().isEmpty()) {
        //mAlarmUtils.scheduleRecurrence(model);
      }
    }
  }
}
