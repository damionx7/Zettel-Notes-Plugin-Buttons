package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import org.eu.thedoc.zettelnotes.interfaces.ScanInterface;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.DatabaseService;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.RegexUtils;
import org.eu.thedoc.zettelnotes.plugins.base.utils.Logger;

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
        Logger.verbose(getClass(), "onScanText " + fileUri);
        //send broadcast only if alarms found
        if (RegexUtils.matches(text)) {
          DatabaseService.startService(context, category, fileTitle, fileUri, text);
          return true;
        }
        return false;
      }

      @Override
      public void onDeleteUris(Context context, String category, List<String> fileUris) {
        Logger.verbose(getClass(), "onDeleteUris");
        if (fileUris.size() > 0) {
          DatabaseService.startService(context, category, new ArrayList<>(fileUris));
        }
      }

      @Override
      public String onProcessText(Context context, String text) {
        return null;
      }
    };
  }

}

