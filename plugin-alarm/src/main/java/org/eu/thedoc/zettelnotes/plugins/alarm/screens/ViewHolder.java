package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.AlarmAdapter.Listener;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.DateTimeHelper;

public class ViewHolder
    extends RecyclerView.ViewHolder {

  private final CardView mCardView;
  private final TextView mCategoryView;
  private final TextView mTextView;
  private final TextView mFileView;
  private final TextView mCalendarView;
  private final int mOverdueColor;
  private final AppCompatImageButton mDelete, mCheck;

  public ViewHolder(
      @NonNull View itemView) {
    super(itemView);
    mCardView = itemView.findViewById(R.id.card_view);
    mCategoryView = itemView.findViewById(R.id.text_view_category);
    mTextView = itemView.findViewById(R.id.text_view_text);
    mFileView = itemView.findViewById(R.id.text_view_file);
    mCalendarView = itemView.findViewById(R.id.text_view_calendar);
    mDelete = itemView.findViewById(R.id.buttonDelete);
    mCheck = itemView.findViewById(R.id.buttonCheck);
    mOverdueColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
  }

  public void bindTo(AlarmModel model, Calendar calendar, Listener listener) {
    if (model != null) {

      boolean isBefore = model.getCalendar().before(calendar);
      if (isBefore) {
        mCalendarView.getCompoundDrawables()[0].setTint(mOverdueColor);
      } else {
        mCalendarView.getCompoundDrawables()[0].clearColorFilter();
      }

      mCheck.setVisibility(model.getType().equals(AlarmModel.TYPE_TASK) && !model.isChecked() ? View.VISIBLE : View.GONE);

      mCardView.setOnClickListener(v -> listener.onClick(model));
      mCardView.setOnLongClickListener(v -> listener.onLongPress(model));
      mDelete.setOnClickListener(v -> listener.onDelete(model));
      mCheck.setOnClickListener(v -> listener.onCheckTask(model));

      mCategoryView.setText(model.getCategory());
      mFileView.setText(model.getFileTitle());
      mTextView.setText(model.getText());
      try {
        mCalendarView.setText(DateTimeHelper.fromCalendar(model.getCalendar()));
      } catch (Exception e) {
        Log.e(getClass().getName(), e.toString());
      }
    }
  }
}
