package org.eu.thedoc.zettelnotes.buttons.todotxt;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  public static final String INTENT_ACTION = "org.eu.thedoc.zettelnotes.intent.buttons.todotxt";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String text = result.getData().getStringExtra(MainActivity.INTENT_EXTRA_TEXT);
      Integer integer = result.getData().getIntExtra(MainActivity.INTENT_EXTRA_INDEX, -1);
      Log.v(getClass().getName(), "Got text");
      if (mCallback != null) {
        if (integer == -1) {
          Log.e(getClass().getName(), "Line index less than zero");
        } else if (text != null && !text.isEmpty()) {
          mCallback.replaceCurrentLine(new Pair<>(integer, text));
        }
      }
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
        Pair<Integer, String> pair = mCallback.getCurrentLine();
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent(INTENT_ACTION)
            .putExtra(MainActivity.INTENT_EXTRA_INDEX, pair.first)
            .putExtra(MainActivity.INTENT_EXTRA_TEXT, pair.second));
      }
    }

    @Override
    public boolean onLongClick() {
      return false;
    }

  };

  @Override
  public String getName() {
    return "TodoTxt";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }

}
