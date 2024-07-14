package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.eu.thedoc.zettelnotes.interfaces.ScanInterface;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.DatabaseService;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.RegexHelper;

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
        Log.v(getClass().getName(), "onScanText " + fileUri);
        //send broadcast only if alarms found
        if (RegexHelper.matches(text)) {
          DatabaseService.startService(context, category, fileTitle, fileUri, text);
          return true;
        }
        return false;
      }

      @Override
      public String onProcessText(Context context, String text) {
        return null;
      }

      @Override
      public void onDeleteUris(Context context, String category, List<String> fileUris) {
        Log.v(getClass().getName(), "onDeleteUris");
        if (!fileUris.isEmpty()) {
          DatabaseService.startService(context, category, new ArrayList<>(fileUris));
        }
      }
    };
  }

}