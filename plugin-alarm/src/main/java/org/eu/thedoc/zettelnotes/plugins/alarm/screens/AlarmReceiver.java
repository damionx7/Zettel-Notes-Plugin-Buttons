package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import com.google.gson.Gson;
import org.eu.thedoc.zettelnotes.broadcasts.AbstractPluginReceiver;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.AlarmHelper;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.NotificationHelper;

public class AlarmReceiver
    extends BroadcastReceiver {

  NotificationHelper mNotificationHelper;
  AlarmHelper mAlarmHelper;

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
    mAlarmHelper = new AlarmHelper(context);

    String json = intent.getStringExtra(DatabaseService.ARGS_CONTENT);
    if (json != null && !json.isEmpty()) {
      //show notification
      AlarmModel model = new Gson().fromJson(json, AlarmModel.class);
      NotificationCompat.Builder notification = mNotificationHelper.buildNotification(getNotificationChannelID(), model.getFileTitle(),
          model.getText(), "", getNotificationTitle(), R.drawable.ic_notification_alarm, false);
      notification.setPriority(NotificationCompat.PRIORITY_HIGH);
      notification.setAutoCancel(true);

      AbstractPluginReceiver.IntentBuilder intentBuilder = AbstractPluginReceiver.IntentBuilder.getInstance().setActionOpenUri().setUri(
          model.getFileUri()).setLineIndexes(model.getIndexes()).setEdit(true).setRepository(model.getCategory());
      Intent pendingIntent = intentBuilder.build();

      PendingIntent pendingViewerIntent = PendingIntent.getActivity(context, model.getId(), pendingIntent,
          PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
      notification.setContentIntent(pendingViewerIntent);

      if (model.getType().equals(AlarmModel.TYPE_TASK) && !model.isChecked()) {
        //show DONE Notification button
        String replacement = model.getText().replace("- [ ] ", "- [x] ");
        intentBuilder.setActionOpenAndReplace(replacement);
        Intent tickTask = intentBuilder.build();
        PendingIntent tickPendingIntent = PendingIntent.getActivity(context, model.getId(), tickTask,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        notification.addAction(new Action(R.drawable.ic_notification_check, "Done", tickPendingIntent));
      }

      mNotificationHelper.showNotification(getNotificationChannelID(), getNotificationChannelName(), model.getId(), notification.build());

      //schedule recurrence
      if (model.getRecurrence() != null && !model.getRecurrence().isEmpty()) {
        mAlarmHelper.scheduleRecurrence(model);
      }
    }
  }
}
