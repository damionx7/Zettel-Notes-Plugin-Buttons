package org.eu.thedoc.zettelnotes.buttons.mermaid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  public static final String INTENT_ACTION_MERMAID = "org.eu.thedoc.zettelnotes.intent.buttons.mermaid";

  private final ActivityResultListener mActivityResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      Uri imageUri = result
          .getData()
          .getData();
      if (imageUri != null) {
        // Offline render: the app imports the PNG itself and inserts the image link
        if (mCallback != null) {
          mCallback.insertUri(imageUri);
        }
        return;
      }
      String txtToReplace = result
          .getData()
          .getStringExtra(ButtonActivity.INTENT_EXTRA_REPLACE_TEXT);
      String txtToInsert = result
          .getData()
          .getStringExtra(ButtonActivity.INTENT_EXTRA_INSERT_TEXT);
      if (mCallback != null) {
        if (txtToReplace != null && !txtToReplace.isEmpty()) {
          mCallback.replaceTextSelected(txtToReplace);
        } else if (txtToInsert != null && !txtToInsert.isEmpty()) {
          mCallback.insertText(txtToInsert);
        }
      }
    } else {
      if (result.getData() != null) {
        String error = result
            .getData()
            .getStringExtra(ButtonActivity.ERROR_STRING);
        Log.e(getClass().getName(), String.valueOf(error));
      }
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mActivityResultListener);
        mCallback.startActivityForResult(
            new Intent(INTENT_ACTION_MERMAID).putExtra(ButtonActivity.INTENT_EXTRA_TEXT_SELECTED, mCallback.getTextSelected(false)));
      }
    }

    @Override
    public boolean onLongClick() {
      // Quick action: replace the selected diagram code with its rendered image link
      if (mCallback != null) {
        String selectedText = mCallback.getTextSelected(false);
        if (selectedText != null && !selectedText.isEmpty()) {
          mCallback.replaceTextSelected(MermaidInk.toImageMarkdown(selectedText));
          return true;
        }
      }
      return false;
    }

  };

  @Override
  public String getName() {
    return "Mermaid";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }

}
