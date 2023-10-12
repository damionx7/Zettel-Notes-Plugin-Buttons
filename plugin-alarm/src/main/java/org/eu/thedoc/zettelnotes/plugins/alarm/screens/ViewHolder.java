package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.Adapter.Listener;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.DateTimeUtils;
import org.eu.thedoc.zettelnotes.plugins.base.utils.Logger;

public class ViewHolder
    extends RecyclerView.ViewHolder {

  private final CardView mCardView;
  private final TextView mCategoryView;
  private final TextView mTextView;
  private final TextView mFileView;
  private final TextView mCalendarView;
  private final int mOverdueColor;

  public ViewHolder(
      @NonNull View itemView) {
    super(itemView);
    mCardView = itemView.findViewById(R.id.card_view);
    mCategoryView = itemView.findViewById(R.id.text_view_category);
    mTextView = itemView.findViewById(R.id.text_view_text);
    mFileView = itemView.findViewById(R.id.text_view_file);
    mCalendarView = itemView.findViewById(R.id.text_view_calendar);
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

      mCardView.setOnClickListener(v -> listener.onClick(model));
      mCardView.setOnLongClickListener(v -> listener.onLongPress(model));
      mCategoryView.setText(model.getCategory());
      mFileView.setText(model.getFileTitle());
      mTextView.setText(model.getText());
      try {
        mCalendarView.setText(DateTimeUtils.fromCalendar(model.getCalendar()));
      } catch (Exception e) {
        Logger.err(getClass(), e.toString());
      }
    }
  }
}
