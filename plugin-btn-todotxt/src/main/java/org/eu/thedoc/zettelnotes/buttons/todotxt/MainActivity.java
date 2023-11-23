package org.eu.thedoc.zettelnotes.buttons.todotxt;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity
    extends AppCompatActivity {

  public static final String INTENT_EXTRA_TEXT = "intent-extra-text";
  public static final String INTENT_EXTRA_INDEX = "intent-extra-index";
  public static final String ERROR_STRING = "intent-error";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    String[] formatOptions = {"Toggle completion", "Split to newlines", "Uppercase", "Lowercase", "Title Case", "Swap Case",
        "Create Task list", "Remove Task list", "Add Month Table", "Add Month Table With Empty Rows", "Add Year Table",
        "Add Year Table with Empty Rows"};
    builder.setItems(formatOptions, (dialog, which) -> {
      String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT);

      if (txtSelected == null || txtSelected.isEmpty()) {
        switch (which) {
          default -> {
            setResult(RESULT_OK, new Intent()
                .putExtra(INTENT_EXTRA_TEXT, txtSelected.replace("(A)", "(B)"))
                .putExtra(INTENT_EXTRA_INDEX, getIntent().getIntExtra(INTENT_EXTRA_INDEX, -1)));
          }
        }
      } else {
        switch (which) {
          default -> {
            setResult(RESULT_OK, new Intent()
                .putExtra(INTENT_EXTRA_TEXT, txtSelected.replace("(A)", "(B)"))
                .putExtra(INTENT_EXTRA_INDEX, getIntent().getIntExtra(INTENT_EXTRA_INDEX, -1)));
          }
        }
      }

      finish();
    });
    builder.setOnCancelListener(dialog -> finish());
    builder.show();
  }

}
