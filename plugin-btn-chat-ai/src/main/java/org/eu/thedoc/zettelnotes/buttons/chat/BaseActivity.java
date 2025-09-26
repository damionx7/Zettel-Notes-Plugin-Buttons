package org.eu.thedoc.zettelnotes.buttons.chat;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseActivity
    extends AppCompatActivity {

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    // Apply insets handling
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
      Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
      boolean isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());

      v.setPadding(systemBars.left, systemBars.top, systemBars.right, isImeVisible ? imeInsets.bottom : systemBars.bottom);
      return WindowInsetsCompat.CONSUMED;
    });
  }
}