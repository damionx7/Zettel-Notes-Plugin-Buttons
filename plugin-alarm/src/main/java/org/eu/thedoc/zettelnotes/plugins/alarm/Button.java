package org.eu.thedoc.zettelnotes.plugins.alarm;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.DateTimeActivity;

public class Button
    extends ButtonInterface {

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String text = result.getData().getStringExtra(DateTimeActivity.EXTRAS_TEXT);
      if (mCallback != null && text != null && !text.isEmpty()) {
        mCallback.insertText(text);
      }
    } else {
      if (result.getData() != null) {
        Log.e(getClass().getName(), "activity result null");
      }
    }
  };

  @Override
  public String getName() {
    return "Alarm";
  }

  @Override
  public Listener getListener() {
    return new Listener() {
      @Override
      public void onClick() {
        if (mCallback != null) {
          String text = mCallback.getTextSelected(false);
          mCallback.setActivityResultListener(mActivityResultListener);
          mCallback.startActivityForResult(
              new Intent("org.eu.thedoc.zettelnotes.intent.buttons.alarm").putExtra(DateTimeActivity.EXTRAS_TEXT, text));
        }
      }

      @Override
      public boolean onLongClick() {
        return false;
      }
    };
  }
}
