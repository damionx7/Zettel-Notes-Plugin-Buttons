package org.eu.thedoc.zettelnotes.buttons.latex;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

public class MainActivity
    extends AppCompatActivity {

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    AppCompatTextView textView = findViewById(R.id.activity_main_text_view);
  }

}


