package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AppDatabaseClient;

public class DatabaseService
    extends Service {

  public static final String ARGS_MODEL = "args-model";
  public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
  private static final String QUERY_INTENT = "org.eu.thedoc.zettelnotes.intent.scan.broadcast";

  private boolean isStarted = false;
  private AppDatabaseClient mClient;
  private NotificationUtils mNotificationUtils;

  public static void stopService(Context context) {
    Intent intent = new Intent(context, DatabaseService.class);
    context.stopService(intent);
  }

  public static void startService(Context context, List<AlarmModel> models) {
    Intent intent = new Intent(QUERY_INTENT);
    intent.setComponent(new ComponentName(DatabaseService.PACKAGE_NAME, DatabaseService.class.getName()));
    intent.putExtra(ARGS_MODEL, new Gson().toJson(models));
    ContextCompat.startForegroundService(context, intent);
  }

  private void showToast(Context context, String text) {
    ContextCompat.getMainExecutor(context).execute(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
  }

  private void addToDatabase(Context context, AlarmModel model) {
    //delete previous alarms
    mClient.getAppDatabase().mAlarmModelDao().delete(model.getCategory(), model.getFile());
    //add new alarms
    mClient.getAppDatabase().mAlarmModelDao().insert(model);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.v("Alarm::DatabaseService", "onCreate");

    mClient = AppDatabaseClient.getInstance(getApplicationContext());
    mNotificationUtils = new NotificationUtils(getApplicationContext());
    mNotificationUtils.createNotificationChannel(getNotificationChannelID(), getNotificationChannelName());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.v("Alarm::DatabaseService", "onStartCommand");

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
    Notification notification = mNotificationUtils.buildNotification(getNotificationChannelID(), getNotificationTitle(), "", "",
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
    return "Alarm!";
  }

  private String getNotificationChannelID() {
    return "scan_plugin_alarm";
  }

  private void processAction(Intent intent) {
    Log.v("Alarm::DatabaseService", "processAction");
    String json = intent.getStringExtra(ARGS_MODEL);
    if (json != null && !json.isEmpty()) {
      List<AlarmModel> models = new Gson().fromJson(json, new TypeToken<List<AlarmModel>>() {}.getType());

    }
  }

}
