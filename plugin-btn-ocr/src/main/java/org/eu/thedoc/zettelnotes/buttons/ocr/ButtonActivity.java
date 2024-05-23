package org.eu.thedoc.zettelnotes.buttons.ocr;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ButtonActivity
    extends AppCompatActivity {

  public static final String ERROR_STRING = "intent-error";
  public static final String RESULT_STRING = "result-string";

  private ProgressBar mProgressBar;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_button);
    mProgressBar = findViewById(R.id.activity_button_progress_bar);
    mProgressBar.setIndeterminate(true);

  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mProgressBar != null) {
      mProgressBar.setVisibility(View.GONE);
    }
  }

}
