package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationUtils {

  private final NotificationManagerCompat mNotificationManager;
  private final Context mContext;

  public NotificationUtils(Context context) {
    mNotificationManager = NotificationManagerCompat.from(context);
    mContext = context;
  }

  public boolean areNotificationsEnabled() {
    return mNotificationManager.areNotificationsEnabled();
  }

  public NotificationCompat.Builder buildNotification(String channelID, String title, String body, String expandedText, String ticker, int icon, boolean silent) {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, channelID);
    notificationBuilder.setSmallIcon(icon);
    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon));
    notificationBuilder.setContentTitle(title);
    notificationBuilder.setContentText(body);
    notificationBuilder.setTicker(ticker);
    notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    notificationBuilder.setWhen(System.currentTimeMillis());
    notificationBuilder.setColor(ContextCompat.getColor(mContext, R.color.ic_launcher_background));
    notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
    notificationBuilder.setAutoCancel(false);
    notificationBuilder.setSilent(silent);
    if (expandedText != null && !expandedText.isEmpty()) {
      notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(expandedText));
    }
    return notificationBuilder;
  }

  public void createNotificationChannel(String channelID, String channelName) {
    if (mNotificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription(channelName);
      mNotificationManager.createNotificationChannel(channel);
    }
  }
}
