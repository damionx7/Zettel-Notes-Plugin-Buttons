package org.eu.thedoc.zettelnotes.buttons.translate;

import static org.eu.thedoc.zettelnotes.buttons.translate.Button.INTENT_ACTION_TRANSLATE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class ProcessTextActivity
    extends AppCompatActivity {

  private static final String CONTENT_STRING = "args-content";

  private String text = "";

  private ActivityResultLauncher<Intent> mIntentActivityResult;

  public void copyToClipboard(Context context, String text) {
    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard != null) {
      ClipData clip = ClipData.newPlainText("text-clip", text);
      clipboard.setPrimaryClip(clip);
      ToastsHelper.showToast(this, "Copied translation to clipboard");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mIntentActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
      if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
        String txtToReplace = result.getData().getStringExtra(TranslateActivity.INTENT_EXTRA_REPLACE_TEXT);
        String txtToInsert = result.getData().getStringExtra(TranslateActivity.INTENT_EXTRA_INSERT_TEXT);
        Log.v(getClass().getName(), "Got text");
        if (!txtToReplace.isEmpty()) {
          copyToClipboard(getApplicationContext(), txtToReplace);
        } else if (!txtToInsert.isEmpty()) {
          copyToClipboard(getApplicationContext(), txtToInsert);
        }
      } else {
        if (result.getData() != null) {
          String error = result.getData().getStringExtra(TranslateActivity.ERROR_STRING);
          ToastsHelper.showToast(this, "Error " + error);
        }
      }
      finish();
    });
    if (savedInstanceState == null) {
      processIntent();
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    processIntent();
  }

  @Override
  protected void onSaveInstanceState(
      @NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(CONTENT_STRING, text);
  }

  @Override
  protected void onRestoreInstanceState(
      @NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState.containsKey(CONTENT_STRING)) {
      text = savedInstanceState.getString(CONTENT_STRING);
    }
  }

  private void processIntent() {
    Intent intent = getIntent();
    if (intent == null) {
      ToastsHelper.showToast(this, "Intent null.");
      Log.e(getClass().getName(), "intent null. aborting...");
      finish();
      return;
    }

    text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
    Intent translateIntent = new Intent(INTENT_ACTION_TRANSLATE).putExtra(TranslateActivity.INTENT_EXTRA_TEXT_SELECTED, text);
    mIntentActivityResult.launch(translateIntent);
    //finish();
  }

}