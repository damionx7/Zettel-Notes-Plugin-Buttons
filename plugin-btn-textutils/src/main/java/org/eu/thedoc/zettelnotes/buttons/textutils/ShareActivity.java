package org.eu.thedoc.zettelnotes.buttons.textutils;

import static org.eu.thedoc.zettelnotes.buttons.textutils.Button.INTENT_ACTION_TEXT_UTILS;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class ShareActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.M)
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            String txtToReplace = result.getData().getStringExtra(TextUtilsActivity.INTENT_EXTRA_REPLACE_TEXT);
            String txtToInsert = result.getData().getStringExtra(TextUtilsActivity.INTENT_EXTRA_INSERT_TEXT);
            Log.v("Ok", "Got text");

            String textToCopy = "";
            if (txtToReplace != null && !txtToReplace.isEmpty()) {
              textToCopy = txtToReplace;
            } else if (txtToInsert != null && !txtToInsert.isEmpty()) {
              textToCopy = txtToInsert;
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
              ClipData clip = ClipData.newPlainText("zn-text-utils", textToCopy);
              clipboard.setPrimaryClip(clip);

              showToast("Copied to clipboard");
            }
          } else {
            if (result.getData() != null) {
              String error = result.getData().getStringExtra(TextUtilsActivity.ERROR_STRING);
              Log.e("Error: ", error);
            }
          }
          finish();
        });

    activityResultLauncher.launch(new Intent(INTENT_ACTION_TEXT_UTILS).putExtra(TextUtilsActivity.INTENT_EXTRA_TEXT_SELECTED, text));
  }

  private void showToast(String text) {
    if (!text.isEmpty()) {
      Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
  }
}
