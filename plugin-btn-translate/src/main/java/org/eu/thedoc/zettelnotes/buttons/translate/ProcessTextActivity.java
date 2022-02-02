package org.eu.thedoc.zettelnotes.buttons.translate;

import static org.eu.thedoc.zettelnotes.buttons.translate.Button.INTENT_ACTION_TRANSLATE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class ProcessTextActivity extends AppCompatActivity {

  private static final String CONTENT_STRING = "args-content";

  private String text = "";

  private ActivityResultLauncher<Intent> mIntentActivityResult;

  public void copyToClipboard (Context context, String text) {
    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard != null) {
      ClipData clip = ClipData.newPlainText("text-clip", text);
      clipboard.setPrimaryClip(clip);
      showToast("Copied translation to clipboard");
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mIntentActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
      if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
        String txtToReplace = result.getData().getStringExtra(TranslateActivity.INTENT_EXTRA_REPLACE_TEXT);
        String txtToInsert = result.getData().getStringExtra(TranslateActivity.INTENT_EXTRA_INSERT_TEXT);
        Log.v("Ok", "Got text");
        if(!txtToReplace.isEmpty()) copyToClipboard(getApplicationContext(), txtToReplace);
        else if(!txtToInsert.isEmpty()) copyToClipboard(getApplicationContext(), txtToInsert);
      } else {
        if (result.getData() != null) {
          String error = result.getData().getStringExtra(TranslateActivity.ERROR_STRING);
          showToast("Error " + error);
        }
      }
      finish();
    });
    if (savedInstanceState == null) processIntent();
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void onNewIntent (Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    processIntent();
  }

  @Override
  protected void onSaveInstanceState (@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(CONTENT_STRING, text);
  }

  @Override
  protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState.containsKey(CONTENT_STRING)) text = savedInstanceState.getString(CONTENT_STRING);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void processIntent () {
    Intent intent = getIntent();
    if (intent == null) {
      showToast("Intent null.");
      Log.e("Process Text", "intent null. aborting...");
      finish();
      return;
    }

    text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
    Intent translateIntent = new Intent(INTENT_ACTION_TRANSLATE).putExtra(TranslateActivity.INTENT_EXTRA_TEXT_SELECTED, text);
    mIntentActivityResult.launch(translateIntent);
    //finish();
  }

  private void showToast (String text) {
    if (!text.isEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }
}
