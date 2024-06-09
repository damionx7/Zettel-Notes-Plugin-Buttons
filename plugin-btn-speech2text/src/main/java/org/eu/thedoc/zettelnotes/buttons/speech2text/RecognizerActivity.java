package org.eu.thedoc.zettelnotes.buttons.speech2text;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RecognizerActivity
    extends AppCompatActivity {

  private SharedPreferences mSharedPreferences;
  private String mLangCode;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_button);
    ProgressBar progressBar = findViewById(R.id.activity_button_progress_bar);
    progressBar.setIndeterminate(true);

    mSharedPreferences = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
    mLangCode = mSharedPreferences.getString(getString(R.string.prefs_language_key), getString(R.string.pref_lang_code_english));

    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLangCode);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, mLangCode);
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text");

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            ArrayList<String> arrayListExtra = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = arrayListExtra.get(0);
            Log.v(getPackageName(), text);

            setResult(RESULT_OK, new Intent().putExtra(Button.RESULT_STRING, text));
            finish();
          } else {
            Log.e(getPackageName(), "Error Result is Null");

            setResult(RESULT_CANCELED, new Intent().putExtra(Button.ERROR_STRING, "Error Result is Null"));
            finish();
          }
        });

    activityResultLauncher.launch(intent);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mLangCode = mSharedPreferences.getString(getString(R.string.prefs_language_key), getString(R.string.pref_lang_code_english));
  }
}
