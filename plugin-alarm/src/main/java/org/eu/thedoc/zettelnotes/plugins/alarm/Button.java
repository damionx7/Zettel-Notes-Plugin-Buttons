package org.eu.thedoc.zettelnotes.plugins.alarm;

import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.DateTimeHelper;

public class Button
    extends ButtonInterface {

  @Override
  public String getName() {
    return "Alarm";
  }

  @Override
  public Listener getListener() {
    return new Listener() {
      @Override
      public void onClick() {
        String date = String.format("[scheduled]: <%s>", DateTimeHelper.fromCurrentCalendar());
        if (mCallback != null) {
          mCallback.insertText(date);
        }
      }

      @Override
      public boolean onLongClick() {
        return false;
      }
    };
  }
}
