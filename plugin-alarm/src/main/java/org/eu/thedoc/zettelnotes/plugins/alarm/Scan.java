package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import java.util.List;
import org.eu.thedoc.zettelnotes.interfaces.ScanInterface;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;

public class Scan
    extends ScanInterface {

  private static final String BROADCAST_ACTION = "org.eu.thedoc.zettelnotes.intent.scan.broadcast";

  @Override
  public String getName() {
    return "Alarm";
  }

  @Override
  public Listener getListener() {
    return new Listener() {
      @Override
      public boolean onScanText(Context context, String repository, String fileName, String text) {
        Log.v(getName(), "onScanText " + fileName);
        List<AlarmModel> models = Regex.getInstance().getModels(repository, fileName, text);
        //send broadcast only if alarms found
        if (models.size() > 0) {
          sendBroadcast(context, models);
          return true;
        }
        return false;
      }

      @Override
      public String onProcessText(Context context, String text) {
        return null;
      }
    };
  }

  private void sendBroadcast(Context context, List<AlarmModel> model) {
    Intent intent = new Intent();
    intent.putExtra(ScanBroadcastReceiver.ARGS_MODEL, new Gson().toJson(model));
    intent.setComponent(new ComponentName(ScanBroadcastReceiver.PACKAGE_NAME, ScanBroadcastReceiver.class.getName()));
    context.sendBroadcast(intent, "permission.zettelnotes.broadcast");
  }

}

