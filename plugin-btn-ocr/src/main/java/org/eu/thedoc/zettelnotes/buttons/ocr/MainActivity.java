package org.eu.thedoc.zettelnotes.buttons.ocr;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity
    extends AppCompatActivity {

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    MaterialButton button = findViewById(R.id.button_submit);

    button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });
  }

}


