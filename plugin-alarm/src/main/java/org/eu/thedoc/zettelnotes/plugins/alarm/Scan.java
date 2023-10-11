package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.Context;
import android.util.Log;
import java.util.List;
import org.eu.thedoc.zettelnotes.interfaces.ScanInterface;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.DatabaseService;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.RegexUtils;

public class Scan
    extends ScanInterface {

  @Override
  public String getName() {
    return "Alarm";
  }

  @Override
  public Listener getListener() {
    return new Listener() {
      @Override
      public boolean onScanText(Context context, String category, String fileUri, String fileTitle, String text) {
        Log.v(getName(), "onScanText " + fileUri);
        List<AlarmModel> models = RegexUtils.parse(category, fileTitle, fileUri, text);
        //send broadcast only if alarms found
        if (models.size() > 0) {
          DatabaseService.startService(context, category, fileTitle, fileUri, models);
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

