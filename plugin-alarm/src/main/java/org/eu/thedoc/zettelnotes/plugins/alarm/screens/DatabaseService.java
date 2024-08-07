package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import org.eu.thedoc.zettelnotes.plugins.alarm.BuildConfig;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.DatabaseRepository;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.NotificationHelper;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.RegexHelper;

public class DatabaseService
    extends Service {

  public static final String ARGS_CONTENT = "args-content";
  public static final String ARGS_CATEGORY = "args-category";
  public static final String ARGS_FILE_URI = "args-file-uri";
  public static final String ARGS_FILE_URIS = "args-file-uris";
  public static final String ARGS_TITLE = "args-title";

  public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

  private static final String QUERY_INTENT_SCAN = "org.eu.thedoc.zettelnotes.plugins.alarm.SCAN";
  private static final String QUERY_INTENT_DELETE_URIS = "org.eu.thedoc.zettelnotes.plugins.alarm.DELETE_URIS";

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
    Log.v(getClass().getName(), "onCreate");

    mNotificationHelper = new NotificationHelper(getApplicationContext());
    mRepository = new DatabaseRepository(getApplicationContext());
    mNotificationHelper.createNotificationChannel(getNotificationChannelID(), getNotificationChannelName());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.v(getClass().getName(), "onStartCommand");

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

  @Override
  public void onTimeout(int startId) {
    super.onTimeout(startId);
    Log.w(super.getClass().getSimpleName(), "%s: >>>>>>>>>>>>>>>>>>>>Short Service Timeout<<<<<<<<<<<<<<<<<<");
    finish();
  }

  private String getNotificationChannelName() {
    return "Scanner";
  }

  private void makeForeground() {
    Notification notification = mNotificationHelper
        .buildNotification(getNotificationChannelID(), getNotificationTitle(), "", "", getNotificationTitle(), getNotificationIcon(), true)
        .build();
    ServiceCompat.startForeground(this, getNotificationID(), notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
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
    Log.v(getClass().getName(), "processAction");
    String action = intent.getAction();
    if (action == null) {
      return;
    }

    if (action.equals(QUERY_INTENT_SCAN)) {
      Log.v(getClass().getName(), QUERY_INTENT_SCAN);
      String text = intent.getStringExtra(ARGS_CONTENT);
      String category = intent.getStringExtra(ARGS_CATEGORY);
      String fileUri = intent.getStringExtra(ARGS_FILE_URI);
      String fileTitle = intent.getStringExtra(ARGS_TITLE);

      if (text != null && !text.isEmpty()) {
        List<AlarmModel> models = RegexHelper.parse(category, fileTitle, fileUri, text);
        //add in repository
        mRepository.addAll(category, fileUri, models);
      }
    } else if (action.equals(QUERY_INTENT_DELETE_URIS)) {
      Log.v(getClass().getName(), QUERY_INTENT_DELETE_URIS);
      String category = intent.getStringExtra(ARGS_CATEGORY);
      ArrayList<String> uris = intent.getStringArrayListExtra(ARGS_FILE_URIS);

      if (category != null && uris != null) {
        Log.v(getClass().getName(), "uris " + uris.size());
        mRepository.deleteAll(category, uris);
      }
    }
    //Stop this service
    finish();
  }

  protected void finish() {
    stopForeground(true);
    stopSelf();
  }

}