package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ScanInterface;

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
      public boolean onScanText(String repository, String fileName, String text) {
        Log.v(getName(), "onScanText " + fileName);
        return true;
      }

      @Override
      public String onProcessText(String text) {
        return null;
      }
    };
  }
}

