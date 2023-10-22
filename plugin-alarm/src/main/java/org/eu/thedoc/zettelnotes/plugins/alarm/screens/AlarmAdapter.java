package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import java.util.Calendar;
import java.util.TimeZone;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.jetbrains.annotations.NotNull;

public class AlarmAdapter
    extends PagingDataAdapter<AlarmModel, ViewHolder> {

  private static final DiffUtil.ItemCallback<AlarmModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<AlarmModel>() {
    @Override
    public boolean areItemsTheSame(AlarmModel oldConcert, AlarmModel newConcert) {
      return oldConcert.getId() == newConcert.getId();
    }

    @Override
    public boolean areContentsTheSame(AlarmModel oldConcert,
        @NotNull AlarmModel newConcert) {
      return oldConcert.equals(newConcert);
    }
  };

  private final Listener mListener;
  private final Calendar mNow;

  public AlarmAdapter(Listener listener) {
    super(DIFF_CALLBACK);
    mListener = listener;
    mNow = Calendar.getInstance(TimeZone.getDefault());
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false));
  }

  @Override
  public void onBindViewHolder(
      @NonNull ViewHolder holder, int position) {
    holder.bindTo(getItem(position), mNow, mListener);
  }

  public AlarmModel getAlarmModel(int pos) {
    return getItem(pos);
  }

  public interface Listener {

    void onClick(AlarmModel model);

    boolean onLongPress(AlarmModel model);

    void onDelete(AlarmModel model);

    void onCheckTask(AlarmModel model);
  }
}
