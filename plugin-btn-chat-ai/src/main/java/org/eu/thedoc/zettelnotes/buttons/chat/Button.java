package org.eu.thedoc.zettelnotes.buttons.chat;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  public static final String INTENT_ACTION = "org.eu.thedoc.zettelnotes.intent.buttons.chat";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String txtToReplace = result.getData().getStringExtra(ChatActivity.INTENT_EXTRA_REPLACE_TEXT);
      String txtToInsert = result.getData().getStringExtra(ChatActivity.INTENT_EXTRA_INSERT_TEXT);
      Log.v("Ok", "Got Text");
      if (mCallback != null) {
        if (txtToReplace != null && !txtToReplace.isEmpty()) {
          mCallback.replaceTextSelected(txtToReplace);
        } else if (txtToInsert != null && !txtToInsert.isEmpty()) {
          mCallback.insertText(txtToInsert);
        }
      }
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(ChatActivity.ERROR_STRING);
        Log.e("Error: ", error);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(new Intent(INTENT_ACTION)
            .putExtra(ChatActivity.INTENT_EXTRA_TEXT_SELECTED, mCallback.getTextSelected(false))
            .putExtra(ChatActivity.INTENT_EXTRA_BUTTON_MODE, true));
      }
    }

    @Override
    public boolean onLongClick() {
      return false;
    }

  };

  @Override
  public String getName() {
    return "Chat GPT";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }


}
