package org.eu.thedoc.zettelnotes.buttons.speech2text;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {


  public static final String ERROR_STRING = "args-error";
  public static final String RESULT_STRING = "args-result";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String text = result.getData().getStringExtra(RESULT_STRING);
      Log.v(getClass().getName(), "Got Data");
      if (mCallback != null && text != null) {
        Log.v(getClass().getName(), "Got Text " + text);
        mCallback.insertText(text);
      }
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(ERROR_STRING);
        Log.e(getClass().getName(), error);
      }
    }
  };

  Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent("org.eu.thedoc.zettelnotes.intent.buttons.speech_to_text"));
      }
    }

    @Override
    public boolean onLongClick() {
      return false;
    }

  };

  @Override
  public String getName() {
    return "Speech to Text";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
