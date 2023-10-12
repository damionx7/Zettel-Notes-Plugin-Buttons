package org.eu.thedoc.zettelnotes.buttons.textutils;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.WordUtils;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class TextUtilsActivity
    extends AppCompatActivity {

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  public static final String ERROR_STRING = "intent-error";
  public static final String LEGACY_SPLIT_NEWLINE = "\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]";

  public static String removeTaskList(String text) {
    List<String> lines = Arrays.asList(text.split(LEGACY_SPLIT_NEWLINE, -1));
    Iterator<String> iterator = lines.iterator();
    StringBuilder builder = new StringBuilder();
    while (iterator.hasNext()) {
      String line = iterator.next();
      //remove all space in start
      line = removeSpaceInStart(line);
      if (line.startsWith("- [ ]") || line.startsWith("- [x]") || line.startsWith("- [X]")) {
        line = line.substring(5);
        //remove all space in start
        line = removeSpaceInStart(line);
      }
      builder.append(line);
      if (iterator.hasNext()) {
        builder.append("\n");
      }
    }
    return builder.toString();
  }

  public static String formatToTaskList(String text) {
    List<String> lines = Arrays.asList(text.split(LEGACY_SPLIT_NEWLINE, -1));
    Iterator<String> iterator = lines.iterator();
    StringBuilder builder = new StringBuilder();
    while (iterator.hasNext()) {
      String line = iterator.next();
      //remove all space in start
      line = removeSpaceInStart(line);
      //remove ☐ / block unicode
      if (line.startsWith("\u2610")) {
        line = line.replace("\u2610", "");
      }
      //add task
      //Log.w("line", line);
      if (!(line.startsWith("- [ ]") || line.startsWith("- [x]") || line.startsWith("- [X]")) && line.length() > 0) {
        //remove all space in start
        line = removeSpaceInStart(line);
        line = "- [ ] " + line;
      }
      //Log.w("line", line);
      builder.append(line);
      if (iterator.hasNext()) {
        builder.append("\n");
      }
    }
    return builder.toString();
  }

  private static String removeSpaceInStart(String line) {
    if (line.startsWith(" ")) {
      line = line.replaceAll("^\\s+", "");
    }
    return line;
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    String[] formatOptions = {"Remove newlines", "Split to newlines", "Uppercase", "Lowercase", "Title Case", "Swap Case",
        "Create Task list", "Remove Task list", "Add Month Table", "Add Month Table With Empty Rows", "Add Year Table",
        "Add Year Table with Empty Rows"};
    builder.setItems(formatOptions, (dialog, which) -> {
      String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);

      if (txtSelected == null || txtSelected.isEmpty()) {
        switch (which) {
          case 8 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT,
              CalendarUtils.getMonthTable(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), false)));
          case 9 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT,
              CalendarUtils.getMonthTable(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), true)));
          case 10 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT,
              CalendarUtils.getYearTable(Calendar.getInstance().get(Calendar.YEAR), false)));
          case 11 -> setResult(RESULT_OK,
              new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, CalendarUtils.getYearTable(Calendar.getInstance().get(Calendar.YEAR), true)));
          default -> {
            setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Select some text"));
            ToastsHelper.showToast(this, "Select some text");
          }
        }
      } else {
        switch (which) {
          case 0 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, removeNewLines(txtSelected)));
          case 1 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, addNewLines(txtSelected)));
          case 2 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, txtSelected.toUpperCase(Locale.ROOT)));
          case 3 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, txtSelected.toLowerCase(Locale.ROOT)));
          case 4 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, WordUtils.capitalizeFully(txtSelected)));
          case 5 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, WordUtils.swapCase(txtSelected)));
          case 6 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, formatToTaskList(txtSelected)));
          case 7 -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, removeTaskList(txtSelected)));
        }
      }
      finish();
    });
    builder.setOnCancelListener(dialog -> finish());
    builder.show();
  }

  private String removeNewLines(String selectedText) {
    return selectedText.replaceAll("\\n+", " ");
  }

  private String addNewLines(String selectedText) {
    return selectedText.replaceAll("\\s+", "\n");
  }

}
