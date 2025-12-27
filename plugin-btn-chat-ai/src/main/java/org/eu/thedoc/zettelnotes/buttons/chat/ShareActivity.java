package org.eu.thedoc.zettelnotes.buttons.chat;

import static org.eu.thedoc.zettelnotes.buttons.chat.Button.INTENT_ACTION;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShareActivity
    extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
    startActivity(new Intent(INTENT_ACTION)
        .putExtra(ChatActivity.INTENT_EXTRA_TEXT_SELECTED, text)
        .putExtra(ChatActivity.INTENT_EXTRA_BUTTON_MODE, true));
    finish();
  }
}