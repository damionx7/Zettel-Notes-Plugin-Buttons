package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;

public class DateTimeActivity
    extends AppCompatActivity
    implements DateTimeDialogFragment.Listener {

  public static final String EXTRAS_TEXT = "extras-text";
  public static final String EXTRAS_INDEXES = "extras-indexes";

  @Override
  public void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(getClass().getName(), "onCreate");

    setContentView(R.layout.date_time_activity);

    String text = getIntent().getStringExtra(EXTRAS_TEXT);
    //show dialog
    if (savedInstanceState == null) {
      DateTimeDialogFragment fragment = DateTimeDialogFragment.getInstance(text);
      fragment.show(getSupportFragmentManager(), "DATE_TIME_DIALOG");
    }
  }

  private int[] getIndexes() {
    return getIntent().getIntArrayExtra(EXTRAS_INDEXES);
  }

  @Override
  public void onDateTimeDialogInsertClick(String text) {
    text = String.format("[scheduled]: <%s>", text);
    setResult(RESULT_OK, new Intent().putExtra(EXTRAS_TEXT, text).putExtra(EXTRAS_INDEXES, getIndexes()));
    finish();
  }

  @Override
  public void onDateTimeDialogCancel() {
    setResult(RESULT_CANCELED);
    finish();
  }
}
