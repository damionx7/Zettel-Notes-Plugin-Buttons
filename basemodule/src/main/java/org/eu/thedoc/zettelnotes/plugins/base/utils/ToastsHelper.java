package org.eu.thedoc.zettelnotes.plugins.base.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class ToastsHelper {

  public static void showToast(Context context, String text) {
    if (text.isBlank()) {
      return;
    }
    ContextCompat.getMainExecutor(context).execute(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
  }

}
