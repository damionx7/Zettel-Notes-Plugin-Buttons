package org.eu.thedoc.zettelnotes.buttons.llm;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;

public class ShareActivity
    extends BaseActivity {

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
    if (text == null || text.length() == 0) {
      finish();
      return;
    }

    startActivity(
        new Intent("org.eu.thedoc.zettelnotes.intent.buttons.llm.menu").putExtra(LlmMenuActivity.INTENT_EXTRA_TEXT, text.toString()));

    finish();
  }
}