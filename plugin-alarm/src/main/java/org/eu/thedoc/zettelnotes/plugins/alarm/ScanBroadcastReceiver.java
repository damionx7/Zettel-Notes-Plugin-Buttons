package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AppDatabaseClient;

public class ScanBroadcastReceiver
    extends android.content.BroadcastReceiver {

  public static final String ARGS_MODEL = "args-model";
  public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
  private AppDatabaseClient mClient;

  private void showToast(Context context, String text) {
    ContextCompat.getMainExecutor(context).execute(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
  }

  private void addToDatabase(Context context, AlarmModel model) {
    if (mClient == null) {
      mClient = AppDatabaseClient.getInstance(context);
    }
    //delete previous alarms
    mClient.getAppDatabase().mAlarmModelDao().delete(model.getCategory(), model.getFile());
    //add new alarms
    mClient.getAppDatabase().mAlarmModelDao().insert(model);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.v("Alarm", "Got Broadcast");
    //showToast(context, "Got Broadcast");

    String json = intent.getStringExtra(ARGS_MODEL);
    if (json != null && !json.isEmpty()) {
      List<AlarmModel> models = new Gson().fromJson(json, new TypeToken<List<AlarmModel>>() {}.getType());
      showToast(context, models.get(0).getText());
    }
  }
}
