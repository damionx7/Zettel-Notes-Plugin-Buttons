package org.eu.thedoc.zettelnotes.buttons.translate;

import android.app.Activity;
import android.content.Intent;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;
import org.eu.thedoc.zettelnotes.plugins.base.utils.Logger;

public class Button
    extends ButtonInterface {

  public static final String INTENT_ACTION_TRANSLATE = "org.eu.thedoc.zettelnotes.intent.buttons.translate";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String txtToReplace = result.getData().getStringExtra(TranslateActivity.INTENT_EXTRA_REPLACE_TEXT);
      String txtToInsert = result.getData().getStringExtra(TranslateActivity.INTENT_EXTRA_INSERT_TEXT);
      Logger.verbose(getClass(), "Got text");
      if (mCallback != null) {
        if (txtToReplace != null && !txtToReplace.isEmpty()) {
          mCallback.replaceTextSelected(txtToReplace);
        } else if (txtToInsert != null && !txtToInsert.isEmpty()) {
          mCallback.insertText(txtToInsert);
        }
      }
    } else {
      if (result.getData() != null) {
        String error = result.getData().getStringExtra(TranslateActivity.ERROR_STRING);
        Logger.err(getClass(), error);
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      Logger.verbose(getClass(), "onClick");
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(
            new Intent(INTENT_ACTION_TRANSLATE).putExtra(TranslateActivity.INTENT_EXTRA_TEXT_SELECTED, mCallback.getTextSelected(true)));
      }
    }

    //todo: use last selected language
    @Override
    public boolean onLongClick() {
      Logger.verbose(getClass(), "onLongClick");
      return false;
    }
  };

  @Override
  public String getName() {
    return "Translate";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
