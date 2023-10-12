package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class ToastsHelper {

  public static void showToast(Context context, String text) {
    ContextCompat.getMainExecutor(context).execute(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
  }

}
