package org.eu.thedoc.zettelnotes.buttons.speedread;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.lifecycle.MutableLiveData;

//https://github.com/syniuhin/Readily/blob/master/ui/src/main/java/com/infmme/readilyapp/ReaderFragment.java
public class SpeedReadActivity extends AppCompatActivity {

  public static final String INTENT_EXTRA = "intent-extra";
  public static final String ERROR_STRING = "intent-error";
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private TextView textView, statusBarTextView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_speed_read);
    textView = findViewById(R.id.textView);
    statusBarTextView = findViewById(R.id.statusBar);

    AppCompatImageButton settingsButton = findViewById(R.id.button);
    settingsButton.setOnClickListener(v -> {
      Intent intent = new Intent();
      intent.setClass(getApplicationContext(), LauncherActivity.class);
      startActivity(intent);
    });

  }

  @Override
  protected void onStart() {
    super.onStart();

    SharedPreferences sharedPreferences = getSharedPreferences(LauncherActivity.PREFS, MODE_PRIVATE);
    int rate = sharedPreferences.getInt("prefs_rate", 1000);
    statusBarTextView.setText(String.format("%s ms", rate));

    String txtSelected = getIntent().getStringExtra(INTENT_EXTRA);
    String[] result = txtSelected.split("\\s");

    for (int i = 0; i < result.length; i++) {
      String string = result[i];
      mHandler.postDelayed(() -> textView.setText(string), (long) i * rate);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    //remove handler callbacks
    mHandler.removeMessages(0);
  }

  private void showToast(String text) {
    if (text.isBlank()) {
      return;
    }
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

}
