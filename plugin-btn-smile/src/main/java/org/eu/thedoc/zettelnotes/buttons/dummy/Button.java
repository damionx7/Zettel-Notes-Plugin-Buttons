package org.eu.thedoc.zettelnotes.buttons.dummy;

import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.insertText("\uD83D\uDE04");
      }
    }

    @Override
    public boolean onLongClick() {
      if (mCallback != null) {
        String selectedText = mCallback.getTextSelected(false);
        if (!selectedText.isEmpty()) {
          mCallback.replaceTextSelected("\uD83D\uDE04");
          return true;
        }
      }
      return false;
    }

  };

  @Override
  public String getName() {
    return "Donation";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
