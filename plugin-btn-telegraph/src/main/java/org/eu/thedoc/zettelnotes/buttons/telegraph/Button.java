package org.eu.thedoc.zettelnotes.buttons.telegraph;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button extends ButtonInterface {

  public static final String INTENT_ACTION_TELEGRAPH = "org.eu.thedoc.zettelnotes.intent.buttons.telegraph";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String txtToInsert = result.getData().getStringExtra(TelegraphActivity.INTENT_EXTRA_INSERT_TEXT);
      if (mCallback != null && txtToInsert != null && !txtToInsert.isEmpty()) {
        mCallback.insertText(txtToInsert);
      }
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(TelegraphActivity.ERROR_STRING);
        Log.e(getClass().getName(), error != null ? error : "Unknown error");
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(
            new Intent(INTENT_ACTION_TELEGRAPH).putExtra(TelegraphActivity.INTENT_EXTRA_TEXT_SELECTED, mCallback.getTextSelected(true)));
      }
    }

    @Override
    public boolean onLongClick() {
      return false;
    }
  };

  @Override
  public String getName() {
    return "Telegraph";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
