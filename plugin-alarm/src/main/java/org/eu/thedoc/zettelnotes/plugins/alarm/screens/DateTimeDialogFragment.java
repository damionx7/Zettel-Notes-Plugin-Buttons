package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.DateTimeHelper;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

public class DateTimeDialogFragment
    extends AppCompatDialogFragment
    implements OnDateSetListener, OnTimeSetListener {

  private static final String ARGS_TEXT = "args-text";
  private EditText mEditText;
  private Listener mListener;

  public static DateTimeDialogFragment getInstance(String text) {
    DateTimeDialogFragment fragment = new DateTimeDialogFragment();
    Bundle args = new Bundle();
    args.putString(ARGS_TEXT, text);
    fragment.setArguments(args);
    return fragment;
  }


  @Override
  public void onAttach(
      @NonNull Context context) {
    //Timber.v("onAttach");
    super.onAttach(context);
    try {
      mListener = getParentFragment() == null ? (Listener) requireActivity() : (Listener) requireParentFragment();
    } catch (ClassCastException e) {
      Log.e(getClass().getName(), e.toString());
      throw new ClassCastException("Calling activity / fragment must implement interface");
    }
  }

  @Override
  public void onDismiss(
      @NonNull DialogInterface dialog) {
    super.onDismiss(dialog);
    mListener.onDateTimeDialogCancel();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(
      @Nullable Bundle savedInstanceState) {
    AlertDialog.Builder builder = new Builder(requireContext());
    mEditText = new EditText(requireContext());
    mEditText.setHint("Scheduled time");
    mEditText.setSingleLine();
    mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    mEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    //set current time as default
    String text = requireArguments().getString(ARGS_TEXT);
    String date = DateTimeHelper.fromCurrentCalendar();
    if (text != null) {
      Matcher matcher = Regex.ALARM.pattern.matcher(text);
      if (matcher.matches()) {
        date = matcher.group(1);
      }
    }
    mEditText.setText(date);

    MaterialButton calendarChip = new MaterialButton(requireContext());
    calendarChip.setText("Calendar");
    calendarChip.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar));
    calendarChip.setOnClickListener(v -> {
      Calendar calendar = DateTimeHelper.getCalendar(mEditText.getText().toString());
      if (calendar == null) {
        calendar = DateTimeHelper.getCurrent();
      }
      DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), 0, this, calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
      datePickerDialog.show();
    });

    MaterialButton timeChip = new MaterialButton(requireContext());
    timeChip.setText("Time");
    timeChip.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_time));
    timeChip.setOnClickListener(v -> {
      Calendar calendar = DateTimeHelper.getCalendar(mEditText.getText().toString(), true);
      TimePickerDialog dialog = new TimePickerDialog(requireContext(), 0, this, calendar.get(Calendar.HOUR_OF_DAY),
          calendar.get(Calendar.MINUTE), false);
      dialog.show();
    });

    ChipGroup chipGroup = new ChipGroup(requireContext());

    EnumSet.allOf(DayChips.class).forEach(value -> {
      Chip idChip = new Chip(requireContext());
      idChip.setText(value.title);
      idChip.setOnClickListener(v -> {
        final Calendar calendar = DateTimeHelper.getCalendar(mEditText.getText().toString(), true);
        switch (value.timeUnit) {
          case Calendar.HOUR_OF_DAY -> calendar.add(Calendar.HOUR_OF_DAY, value.value);
          case Calendar.DAY_OF_YEAR -> calendar.add(Calendar.DAY_OF_YEAR, value.value);
          case Calendar.WEEK_OF_YEAR -> calendar.add(Calendar.WEEK_OF_YEAR, value.value);
          case Calendar.MONTH -> calendar.add(Calendar.MONTH, value.value);
          case -1 -> calendar.setTime(DateTimeHelper.getCurrent().getTime());
        }
        mEditText.setText(DateTimeHelper.fromCalendar(calendar));
      });
      chipGroup.addView(idChip);
    });

    EnumSet.allOf(TimeChips.class).forEach(value -> {
      Chip idChip = new Chip(requireContext());
      idChip.setText(value.title);
      idChip.setOnClickListener(v -> {
        final Calendar calendar = DateTimeHelper.getCalendar(mEditText.getText().toString(), true);
        calendar.set(Calendar.HOUR_OF_DAY, value.hour);
        calendar.set(Calendar.MINUTE, value.minute);
        mEditText.setText(DateTimeHelper.fromCalendar(calendar));
      });
      chipGroup.addView(idChip);
    });

    LinearLayout container = new LinearLayout(requireContext());
    container.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    params.leftMargin = requireContext().getResources().getDimensionPixelSize(R.dimen.dialog_margin_sixteen_dp);
    params.topMargin = requireContext().getResources().getDimensionPixelSize(R.dimen.dialog_margin_eight_dp);
    params.rightMargin = requireContext().getResources().getDimensionPixelSize(R.dimen.dialog_margin_sixteen_dp);

    mEditText.setLayoutParams(params);
    calendarChip.setLayoutParams(params);
    timeChip.setLayoutParams(params);
    chipGroup.setLayoutParams(params);

    container.addView(mEditText);
    container.addView(calendarChip);
    container.addView(timeChip);
    container.addView(chipGroup);

    builder.setPositiveButton("Submit", null);
    builder.setView(container);

    AlertDialog dialog = builder.create();

    dialog.setOnShowListener(dialog1 -> {
      mEditText.setSelection(mEditText.getText().length());
      // separate on positive button click to show error and no dismiss dialog at same time
      // https://stackoverflow.com/a/46440572
      Button positiveButton = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
      positiveButton.setOnClickListener(v -> {
        mListener.onDateTimeDialogInsertClick(mEditText.getText().toString());
        dismiss();
      });

      //bind enter key with ok btn
      mEditText.setOnEditorActionListener((v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          return positiveButton.callOnClick();
        }
        return false;
      });
    });
    return dialog;
  }

  @Override
  public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
    Calendar cal = DateTimeHelper.getCalendar(mEditText.getText().toString());
    if (cal != null) {
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.MONTH, month);
      cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
      mEditText.setText(DateTimeHelper.fromCalendar(cal));
    }
    //ToastsHelper.showToast(getContext(), String.format("%s-%s-%s", year, month, dayOfMonth));
  }

  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    Calendar cal = DateTimeHelper.getCalendar(mEditText.getText().toString());
    if (cal != null) {
      cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
      cal.set(Calendar.MINUTE, minute);
      String value = DateTimeHelper.fromCalendar(cal);
      mEditText.setText(value);
    }
    //ToastsHelper.showToast(getContext(), String.format("%s:%s", hourOfDay, minute));
  }

  private enum DayChips {

    TODAY("Today", 1, -1),
    TOMORROW("Tomorrow", 1, Calendar.DAY_OF_YEAR),
    NEXT_HOUR("Next Hour", 1, Calendar.HOUR_OF_DAY),
    NEXT_WEEK("Next Week", 1, Calendar.WEEK_OF_YEAR),
    NEXT_MONTH("Next Month", 1, Calendar.MONTH);

    final String title;
    final int value;
    final int timeUnit;

    DayChips(String title, int value, int timeUnit) {
      this.title = title;
      this.value = value;
      this.timeUnit = timeUnit;
    }
  }

  private enum TimeChips {

    MORNING("Morning", 9, 0),
    AFTERNOON("Afternoon", 13, 0),
    EVENING("Evening", 17, 0),
    NIGHT("Night", 21, 0);

    final String title;
    final int hour;
    final int minute;

    TimeChips(String title, int hour, int minute) {
      this.title = title;
      this.hour = hour;
      this.minute = minute;
    }
  }

  public interface Listener {

    void onDateTimeDialogInsertClick(String text);

    void onDateTimeDialogCancel();
  }

}
