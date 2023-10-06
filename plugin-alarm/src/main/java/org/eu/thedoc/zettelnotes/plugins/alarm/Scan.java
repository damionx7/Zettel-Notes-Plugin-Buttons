package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.Context;
import android.util.Log;
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
          DatabaseService.startService(context, models);
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

}

