package org.eu.thedoc.zettelnotes.buttons.latex;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String text = result.getData().getStringExtra(ButtonActivity.DATA_STRING);
      Log.v(getClass().getName(), "Got Data");
      if (mCallback != null && text != null) {
        Log.v(getClass().getName(), "Got Text " + text);
        mCallback.insertText(text);
      }
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(ButtonActivity.ERROR_STRING);
        Log.e(getClass().getName(), error);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent("org.eu.thedoc.zettelnotes.intent.buttons.latex"));
      }
    }

    @Override
    public boolean onLongClick() {
      return false;
    }

  };

  @Override
  public String getName() {
    return "Latex";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }


}
