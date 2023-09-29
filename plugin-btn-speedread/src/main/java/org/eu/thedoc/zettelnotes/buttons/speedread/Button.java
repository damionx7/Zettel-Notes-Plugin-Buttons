package org.eu.thedoc.zettelnotes.buttons.speedread;

import android.content.Intent;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button extends ButtonInterface {

  public static final String INTENT_ACTION = "org.eu.thedoc.zettelnotes.intent.buttons.speedread";

  private final Listener mListener = new Listener() {
    @Override
    public void onClick () {
      if (mCallback != null) {
        mCallback.startActivityForResult(
            new Intent(INTENT_ACTION).putExtra(SpeedReadActivity.INTENT_EXTRA, mCallback.getTextSelected(true)));
      }
    }

    @Override
    public boolean onLongClick () {
      return false;
    }

  };

  @Override
  public String getName() {
    return "Speed Read";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
