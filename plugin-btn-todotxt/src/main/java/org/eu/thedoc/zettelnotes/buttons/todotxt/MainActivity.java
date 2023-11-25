package org.eu.thedoc.zettelnotes.buttons.todotxt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity
    extends AppCompatActivity {

  public static final String INTENT_EXTRA_TEXT = "intent-extra-text";
  public static final String INTENT_EXTRA_INDEX = "intent-extra-index";
  public static final String ERROR_STRING = "intent-error";

  public static final String CHOICE_PRIORITY = "Priority";
  public static final String CHOICE_TOGGLE = "Toggle";
  public static final String CHOICE_DUE_DATE = "Due Date";
  public static final String CHOICE_COMPLETION_DATE = "Completion Date";
  private Todo mTodo;

  private static String time2human(Long time) {
    String pattern = "yyyy-MM-dd";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
    return simpleDateFormat.format(new Date(time));
  }

  private Long humanTimeToLong(String time) {
    try {
      String pattern = "yyyy-MM-dd";
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
      return simpleDateFormat.parse(time).getTime();
    } catch (Exception e) {
      Log.e(getClass().getName(), e.toString());
    }
    return null;
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String text = getIntent().getStringExtra(INTENT_EXTRA_TEXT);
    if (text == null || text.isEmpty()) {
      showNewTaskDialog();
    } else {

      mTodo = Todo.parse(text);

      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
      String[] formatOptions = {CHOICE_TOGGLE, CHOICE_PRIORITY, CHOICE_DUE_DATE, CHOICE_COMPLETION_DATE};
      builder.setItems(formatOptions, (dialog, which) -> {
        switch (formatOptions[which]) {
          case CHOICE_TOGGLE -> toggleChecked();
          case CHOICE_PRIORITY -> showPriorityDialog();
          case CHOICE_DUE_DATE -> showDueDateDialog();
          case CHOICE_COMPLETION_DATE -> showCompletionDateDialog();
        }
      });

      builder.setOnCancelListener(dialog -> finish());
      builder.show();
    }
  }

  private void showNewTaskDialog() {
    MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
    alert.setTitle("New Todo");

    EditText editText = new EditText(this, null);
    editText.setSingleLine(true);
    editText.setText(String.format("(A) %s ", time2human(System.currentTimeMillis())));
    editText.setHint("Todo");
    editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    LinearLayout linearLayout = new LinearLayout(this);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    int dp16 = getResources().getDimensionPixelSize(R.dimen.dp16);
    int dp8 = getResources().getDimensionPixelSize(R.dimen.dp8);
    params.setMargins(dp16, dp8, dp16, dp8);
    linearLayout.setLayoutParams(params);
    editText.setLayoutParams(params);
    linearLayout.addView(editText);
    alert.setView(linearLayout);
    alert.setPositiveButton("Save", null);

    AlertDialog dialog = alert.create();
    dialog.getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    dialog.setOnShowListener(dialog1 -> {
      editText.setSelection(editText.getText().length());
      editText.requestFocus();
      Button positiveButton = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
      positiveButton.setOnClickListener(v -> {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
          editText.requestFocus();
        } else {
          setResult(text);
        }
      });
      //bind enter key with ok btn
      editText.setOnEditorActionListener((view, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          positiveButton.callOnClick();
          return true;
        }
        return false;
      });
    });
    dialog.setOnCancelListener(dialog12 -> finish());
    dialog.show();
  }

  private void showCompletionDateDialog() {
    Long selectionDate = humanTimeToLong(mTodo.completionDate);
    Long constraintDate = humanTimeToLong(mTodo.dueDate);

    MaterialDatePicker.Builder<Long> pickerBuilder = MaterialDatePicker.Builder.datePicker().setTitleText("Completion Date").setSelection(
        selectionDate);
    if (selectionDate != null) {
      pickerBuilder.setSelection(selectionDate);
    }
    if (constraintDate != null) {
      pickerBuilder.setCalendarConstraints(new CalendarConstraints.Builder().setStart(constraintDate).build());
    }
    MaterialDatePicker<Long> picker = pickerBuilder.build();
    picker.addOnPositiveButtonClickListener(selection -> {
      if (selection != null) {
        Log.v(getClass().getName(), "completion date selection " + selection);
        mTodo.completionDate = time2human(selection);
        mTodo.isChecked = true;
        setResult(Todo.toString(mTodo));
      }
      finish();
    });
    picker.addOnCancelListener(dialog -> finish());
    picker.show(getSupportFragmentManager(), "date-picker");
  }

  private void showDueDateDialog() {
    Long selectionDate = humanTimeToLong(mTodo.dueDate);

    MaterialDatePicker.Builder<Long> pickerBuilder = MaterialDatePicker.Builder.datePicker().setTitleText("Due Date");
    if (selectionDate != null) {
      pickerBuilder.setSelection(selectionDate);
    }
    MaterialDatePicker<Long> picker = pickerBuilder.build();
    picker.addOnPositiveButtonClickListener(selection -> {
      if (selection != null) {
        Log.v(getClass().getName(), "due date selection " + selection);
        mTodo.dueDate = time2human(selection);
        setResult(Todo.toString(mTodo));
      }
      finish();
    });
    picker.addOnCancelListener(dialog -> finish());
    picker.show(getSupportFragmentManager(), "date-picker");
  }

  private void toggleChecked() {
    mTodo.isChecked = !mTodo.isChecked;
    setResult(Todo.toString(mTodo));
  }

  private void showPriorityDialog() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    String[] priorities = {"A", "B", "C", "D", "E"};
    builder.setItems(priorities, (dialog1, which1) -> {
      mTodo.priority = priorities[which1];
      setResult(Todo.toString(mTodo));
    });
    builder.show();
  }

  private void setResult(String text) {
    setResult(RESULT_OK,
        new Intent().putExtra(INTENT_EXTRA_TEXT, text).putExtra(INTENT_EXTRA_INDEX, getIntent().getIntExtra(INTENT_EXTRA_INDEX, -1)));
    finish();
  }
}
