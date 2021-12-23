package org.eu.thedoc.zettelnotes.buttons.textutils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.text.WordUtils;

import java.util.Locale;

public class TextUtilsActivity extends AppCompatActivity {

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String ERROR_STRING = "intent-error";

  @Override
  protected void onCreate (@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    String[] formatOptions = {"Remove newlines", "Split to newlines", "Uppercase", "Lowercase", "Title Case", "Swap Case"};
    builder.setItems(formatOptions, (dialog, which) -> {
      String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
      if (txtSelected.isEmpty()) {
        setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Select some text"));
        showToast("Select some text");
      } else {
        switch (which) {
          case 0:
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, removeNewLines(txtSelected)));
            break;
          case 1:
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, addNewLines(txtSelected)));
            break;
          case 2:
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, txtSelected.toUpperCase(Locale.ROOT)));
            break;
          case 3:
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, txtSelected.toLowerCase(Locale.ROOT)));
            break;
          case 4:
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, WordUtils.capitalizeFully(txtSelected)));
            break;
          case 5:
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, WordUtils.swapCase(txtSelected)));
            break;
        }
      }
      finish();
    });
    builder.setOnCancelListener(dialog -> finish());
    builder.show();
  }


  private String removeNewLines (String selectedText) {
    return selectedText.replaceAll("\\n+", " ");
  }

  private String addNewLines (String selectedText) {
    return selectedText.replaceAll("\\s+", "\n");
  }

  private void showToast (String text) {
    if (!text.isEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

}
