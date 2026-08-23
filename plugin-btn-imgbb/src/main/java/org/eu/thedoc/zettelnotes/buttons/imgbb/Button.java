package org.eu.thedoc.zettelnotes.buttons.imgbb;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  public static final String INTENT_ACTION = "org.eu.thedoc.zettelnotes.intent.buttons.imgbb";
  public static final String INTENT_ACTION_SETTINGS = "org.eu.thedoc.zettelnotes.intent.buttons.imgbb.settings";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String markdown = result
          .getData()
          .getStringExtra(MainActivity.INTENT_EXTRA_INSERT_TEXT);
      Log.v(getClass().getName(), "Got upload result");
      if (mCallback != null && markdown != null && !markdown.isEmpty()) {
        mCallback.insertText(markdown);
      }
    } else {
      if (result.getData() != null) {
        String error = result
            .getData()
            .getStringExtra(MainActivity.ERROR_STRING);
        Log.e(getClass().getName(), error);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent(INTENT_ACTION));
      }
    }

    @Override
    public boolean onLongClick() {
      if (mCallback != null) {
        // Long-press jumps straight to the API key settings screen.
        mCallback.setActivityResultListener(result -> { /* no result expected */ });
        mCallback.startActivityForResult(new Intent(INTENT_ACTION_SETTINGS));
        return true;
      }
      return false;
    }
  };

  @Override
  public String getName() {
    return "ImgBB Upload";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}