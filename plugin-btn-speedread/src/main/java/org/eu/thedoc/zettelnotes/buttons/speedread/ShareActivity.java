package org.eu.thedoc.zettelnotes.buttons.speedread;

import static org.eu.thedoc.zettelnotes.buttons.speedread.Button.INTENT_ACTION;

import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class ShareActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.M)
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
    startActivity(new Intent(INTENT_ACTION).putExtra(SpeedReadActivity.INTENT_EXTRA, text));
    finish();
  }
}
