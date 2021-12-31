package org.eu.thedoc.zettelnotes.buttons.speech2text;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

import java.util.ArrayList;
import java.util.Locale;

public class Button extends ButtonInterface {

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      ArrayList<String> arrayListExtra = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      String text = arrayListExtra.get(0);
      Log.v("Text", text);
      if (mCallback != null) mCallback.insertText(text);
    }
  };

  Listener mListener = new Listener() {
    @Override
    public void onClick () {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");
        try {
          mCallback.startActivityForResult(intent);
        } catch (Exception e) {
          Log.e("Error", e.getMessage());
        }
      }
    }

    @Override
    public boolean onLongClick () {
      return false;
    }

    @Override
    public ActivityResultListener getActivityResultListener () {
      return null;
    }
  };

  @Override
  public String getName () {
    return "Speech to Text";
  }

  @Override
  public Listener getListener () {
    return mListener;
  }
}
