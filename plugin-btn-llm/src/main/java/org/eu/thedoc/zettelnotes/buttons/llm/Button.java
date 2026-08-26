package org.eu.thedoc.zettelnotes.buttons.llm;

import android.app.Activity;
import android.content.Intent;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  private final ActivityResultListener mResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String output = result
          .getData()
          .getStringExtra(LlmMenuActivity.INTENT_EXTRA_RESULT);
      if (mCallback != null && output != null && !output.isEmpty()) {
        mCallback.replaceTextSelected(output);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback == null) {
        return;
      }
      String text = mCallback.getTextSelected(true);
      if (text == null || text.isEmpty()) {
        return;
      }
      mCallback.setActivityResultListener(mResultListener);
      Intent intent = new Intent("org.eu.thedoc.zettelnotes.intent.buttons.llm.menu");
      intent.putExtra(LlmMenuActivity.INTENT_EXTRA_TEXT, text);
      mCallback.startActivityForResult(intent);
    }

    @Override
    public boolean onLongClick() {
      if (mCallback == null) {
        return false;
      }
      String text = mCallback.getTextSelected(true);
      if (text == null || text.isEmpty()) {
        return false;
      }
      mCallback.setActivityResultListener(mResultListener);
      Intent intent = new Intent("org.eu.thedoc.zettelnotes.intent.buttons.llm.process");
      intent.putExtra(LlmProcessActivity.INTENT_EXTRA_TEXT, text);
      intent.putExtra(LlmProcessActivity.INTENT_EXTRA_ACTION, LlmAction.SUMMARIZE.name());
      mCallback.startActivityForResult(intent);
      return true;
    }
  };

  @Override
  public String getName() {
    return "AI Actions";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}