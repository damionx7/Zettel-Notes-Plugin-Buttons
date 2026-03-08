package org.eu.thedoc.zettelnotes.buttons.latex;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;

public class MainActivity
    extends BaseActivity {

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    AppCompatTextView textView = findViewById(R.id.activity_main_text_view);
  }

}


