package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import org.eu.thedoc.zettelnotes.plugins.alarm.BuildConfig;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.DatabaseRepository;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.Logger;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.NotificationHelper;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.RegexUtils;

public class DatabaseService
    extends Service {

  public static final String ARGS_CONTENT = "args-content";
  public static final String ARGS_CATEGORY = "args-category";
  public static final String ARGS_FILE_URI = "args-file-uri";
  public static final String ARGS_FILE_URIS = "args-file-uris";
  public static final String ARGS_TITLE = "args-title";

  public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

  private static final String QUERY_INTENT_SCAN = "org.eu.thedoc.zettelnotes.plugins.alarm.SCAN";
  private static final String QUERY_INTENT_DELETE_URIS = "org.eu.thedoc.zettelnotes.plugins.scan.DELETE_URIS";

  private boolean isStarted = false;

  private NotificationHelper mNotificationHelper;
  private DatabaseRepository mRepository;

  public static void stopService(Context context) {
    Intent intent = new Intent(context, DatabaseService.class);
    context.stopService(intent);
  }

  public static void startService(Context context, String category, String fileTitle, String fileUri, String text) {
    Intent intent = new Intent(QUERY_INTENT_SCAN);
    intent.setComponent(new ComponentName(DatabaseService.PACKAGE_NAME, DatabaseService.class.getName()));
    intent.putExtra(ARGS_CONTENT, text);
    intent.putExtra(ARGS_CATEGORY, category);
    intent.putExtra(ARGS_FILE_URI, fileUri);
    intent.putExtra(ARGS_TITLE, fileTitle);
    ContextCompat.startForegroundService(context, intent);
  }

  public static void startService(Context context, String category, ArrayList<String> fileUris) {
    Intent intent = new Intent(QUERY_INTENT_DELETE_URIS);
    intent.setComponent(new ComponentName(DatabaseService.PACKAGE_NAME, DatabaseService.class.getName()));
    intent.putExtra(ARGS_CATEGORY, category);
    intent.putStringArrayListExtra(ARGS_FILE_URIS, fileUris);
    ContextCompat.startForegroundService(context, intent);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Logger.verbose(getClass(), "onCreate");

    mNotificationHelper = new NotificationHelper(getApplicationContext());
    mRepository = new DatabaseRepository(getApplicationContext());
    mNotificationHelper.createNotificationChannel(getNotificationChannelID(), getNotificationChannelName());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Logger.verbose(getClass(), "onStartCommand");

    if (!isStarted) {
      makeForeground();
      isStarted = true;
    }
    if (intent != null) {
      processAction(intent);
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  private String getNotificationChannelName() {
    return "Scanner";
  }

  private void makeForeground() {
    Notification notification = mNotificationHelper.buildNotification(getNotificationChannelID(), getNotificationTitle(), "", "",
        getNotificationTitle(), getNotificationIcon(), true).build();
    startForeground(getNotificationID(), notification);
  }

  private int getNotificationID() {
    return 1;
  }

  private int getNotificationIcon() {
    return R.drawable.ic_notification_alarm;
  }

  private String getNotificationTitle() {
    return "Adding alarms";
  }

  private String getNotificationChannelID() {
    return "channel_scan";
  }

  private void processAction(Intent intent) {
    Logger.verbose(getClass(), "processAction");
    String action = intent.getAction();
    if (action == null) {
      return;
    }

    if (action.equals(QUERY_INTENT_SCAN)) {
      Logger.verbose(getClass(), QUERY_INTENT_SCAN);
      String text = intent.getStringExtra(ARGS_CONTENT);
      String category = intent.getStringExtra(ARGS_CATEGORY);
      String fileUri = intent.getStringExtra(ARGS_FILE_URI);
      String fileTitle = intent.getStringExtra(ARGS_TITLE);

      if (text != null && !text.isEmpty()) {
        List<AlarmModel> models = RegexUtils.parse(category, fileTitle, fileUri, text);
        //add in repository
        mRepository.addAll(category, fileUri, models);
      }
    } else if (action.equals(QUERY_INTENT_DELETE_URIS)) {
      Logger.verbose(getClass(), QUERY_INTENT_DELETE_URIS);

      String category = intent.getStringExtra(ARGS_CATEGORY);
      ArrayList<String> uris = intent.getStringArrayListExtra(ARGS_FILE_URIS);

      if (category != null && uris != null) {
        Logger.verbose(getClass(), "uris " + uris.size());
        mRepository.deleteAll(category, uris);
      }
    }
  }

}
