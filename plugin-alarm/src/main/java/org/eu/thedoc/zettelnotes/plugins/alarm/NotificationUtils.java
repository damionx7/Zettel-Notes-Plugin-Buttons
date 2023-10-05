package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.Context;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {

  private final NotificationManagerCompat manager;

  public NotificationUtils(Context context) {
    manager = NotificationManagerCompat.from(context);
  }

  public boolean areNotificationsEnabled() {
    return manager.areNotificationsEnabled();
  }

}
