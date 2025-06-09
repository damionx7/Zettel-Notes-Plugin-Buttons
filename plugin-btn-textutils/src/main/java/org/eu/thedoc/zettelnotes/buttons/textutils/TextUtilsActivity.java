package org.eu.thedoc.zettelnotes.buttons.textutils;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
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

  public static String sortLines(String text, boolean ascending) {
    if (StringUtils.isBlank(text)) {
      return text;
    }

    List<String> lines = Arrays.asList(StringUtils.split(text, "\n"));
    lines.sort(ascending ? String.CASE_INSENSITIVE_ORDER : Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));

    return StringUtils.join(lines, "\n");
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setItems(FormatOption.LIST, (dialog, which) -> {
      String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
      String option = FormatOption.LIST[which];
      if (txtSelected == null || txtSelected.isEmpty()) {
        switch (option) {
          case FormatOption.ADD_MONTH_TABLE -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT,
              CalendarUtils.getMonthTable(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), false)));
          case FormatOption.ADD_MONTH_TABLE_WITH_EMPTY_ROWS -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT,
              CalendarUtils.getMonthTable(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), true)));
          case FormatOption.ADD_YEAR_TABLE -> setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT,
              CalendarUtils.getYearTable(Calendar.getInstance().get(Calendar.YEAR), false)));
          case FormatOption.ADD_YEAR_TABLE_WITH_EMPTY_ROWS -> setResult(RESULT_OK,
              new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, CalendarUtils.getYearTable(Calendar.getInstance().get(Calendar.YEAR), true)));
          default -> {
            setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Select some text"));
            ToastsHelper.showToast(this, "Select some text");
          }
        }
      } else {
        switch (option) {
          case FormatOption.REMOVE_NEWLINES ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, removeNewLines(txtSelected)));
          case FormatOption.SPLIT_TO_NEWLINES ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, addNewLines(txtSelected)));
          case FormatOption.UPPERCASE ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, txtSelected.toUpperCase(Locale.ROOT)));
          case FormatOption.LOWERCASE ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, txtSelected.toLowerCase(Locale.ROOT)));
          case FormatOption.TITLE_CASE ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, WordUtils.capitalizeFully(txtSelected)));
          case FormatOption.SWAP_CASE ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, WordUtils.swapCase(txtSelected)));
          case FormatOption.CREATE_TASK_LIST ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, formatToTaskList(txtSelected)));
          case FormatOption.REMOVE_TASK_LIST ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, removeTaskList(txtSelected)));
          case FormatOption.SORT_ASC ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, sortLines(txtSelected, true)));
          case FormatOption.SORT_DESC ->
              setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, sortLines(txtSelected, false)));
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

  private @interface FormatOption {

    String REMOVE_NEWLINES = "Join Lines";
    String SPLIT_TO_NEWLINES = "Split into Lines";
    String UPPERCASE = "Convert to Uppercase";
    String LOWERCASE = "Convert to Lowercase";
    String TITLE_CASE = "Convert to Title Case";
    String SWAP_CASE = "Swap Letter Case";
    String CREATE_TASK_LIST = "Convert to Task List";
    String REMOVE_TASK_LIST = "Remove Task List";
    String SORT_ASC = "Sort Asc";
    String SORT_DESC = "Sort Desc";
    String ADD_MONTH_TABLE = "Insert Month Table";
    String ADD_MONTH_TABLE_WITH_EMPTY_ROWS = "Insert Month Table (Empty Rows)";
    String ADD_YEAR_TABLE = "Insert Year Table";
    String ADD_YEAR_TABLE_WITH_EMPTY_ROWS = "Insert Year Table (Empty Rows)";

    String[] LIST = {FormatOption.REMOVE_NEWLINES, FormatOption.SPLIT_TO_NEWLINES, FormatOption.UPPERCASE, FormatOption.LOWERCASE,
        FormatOption.TITLE_CASE, FormatOption.SWAP_CASE, FormatOption.CREATE_TASK_LIST, FormatOption.REMOVE_TASK_LIST,
        FormatOption.SORT_ASC, FormatOption.SORT_DESC, FormatOption.ADD_MONTH_TABLE, FormatOption.ADD_MONTH_TABLE_WITH_EMPTY_ROWS,
        FormatOption.ADD_YEAR_TABLE, FormatOption.ADD_YEAR_TABLE_WITH_EMPTY_ROWS};

  }

}