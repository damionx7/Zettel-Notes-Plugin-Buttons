package org.eu.thedoc.zettelnotes.buttons.anki;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  public static final String INTENT_ACTION = "org.eu.thedoc.zettelnotes.intent.buttons.anki";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      Log.v(getClass().getName(), "Success.");
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(MainActivity.ERROR_STRING);
        Log.e(getClass().getName(), error);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        String text = mCallback.getTextSelected(true);
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent(INTENT_ACTION).putExtra(MainActivity.EXTRA_TEXT_SELECTED, text));
      }
    }

    @Override
    public boolean onLongClick() {
      return false;
    }

  };

  @Override
  public String getName() {
    return "Anki";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}