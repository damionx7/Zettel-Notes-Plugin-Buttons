package org.eu.thedoc.zettelnotes.buttons.location;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button extends ButtonInterface {

  public static final String INTENT_ACTION_LOC = "org.eu.thedoc.zettelnotes.intent.buttons.location";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String location = result.getData().getStringExtra(LocationActivity.INTENT_EXTRA_LOCATION);
      Log.v("Ok", "Got Location");
      if (mCallback != null && location != null && !location.isEmpty()) {
        mCallback.insertText(location);
      }
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(LocationActivity.ERROR_STRING);
        Log.e("Error: ", error);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick () {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent(INTENT_ACTION_LOC));
      }
    }

    @Override
    public boolean onLongClick () {
      return false;
    }

  };

  @Override
  public String getName () {
    return "Location";
  }

  @Override
  public Listener getListener () {
    return mListener;
  }


}
