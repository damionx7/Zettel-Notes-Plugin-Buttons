package org.eu.thedoc.zettelnotes.buttons.ocr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String text = result.getData().getStringExtra(RESULT_STRING);
            setResult(RESULT_OK, new Intent().putExtra(RESULT_STRING, text));
            finish();
          } else {
            setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Error Result is Null"));
            finish();
          }
        });
    SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
    Intent intent = MainActivity.IntentBuilder
        .getInstance(getApplicationContext())
        .setCamera(sharedPreferences.getBoolean(getString(R.string.prefs_plugin_start_with_camera_key), true))
        .build();
    activityResultLauncher.launch(intent);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mProgressBar != null) {
      mProgressBar.setVisibility(View.GONE);
    }
  }

}
