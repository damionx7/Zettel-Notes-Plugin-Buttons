package org.eu.thedoc.zettelnotes.buttons.latex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Adapter
    extends RecyclerView.Adapter<Adapter.ViewHolder> {

  private final LinkedHashMap<String, String> mHashMap;
  private final List<String> mList;

  private final Listener mListener;
  public int mScrollPosition;

  public Adapter(Listener listener) {
    mListener = listener;
    mList = new ArrayList<>();
    mHashMap = new LinkedHashMap<>();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_button, parent, false));
  }

  @Override
  public void onBindViewHolder(
      @NonNull ViewHolder holder, int position) {
    String desc = mList.get(position);
    String symbol = mHashMap.get(desc);
    if (desc != null && symbol != null) {
      holder.mButton.setText(symbol);
      holder.mButton.setOnClickListener(v -> mListener.onClick(symbol, desc));
      holder.mButton.setTextSize(24f);

      holder.mButton.setContentDescription(desc);
      TooltipCompat.setTooltipText(holder.mButton, desc);
    }
  }

  @Override
  public int getItemCount() {
    return mList.size();
  }


  public void submitData(LinkedHashMap<String, String> treeMap) {
    mHashMap.putAll(treeMap);
    mList.addAll(mHashMap.keySet());
  }

  public interface Listener {

    void onClick(String symbol, String desc);
  }

  public static class ViewHolder
      extends RecyclerView.ViewHolder {

    private final AppCompatButton mButton;

    public ViewHolder(
        @NonNull View itemView) {
      super(itemView);
      mButton = itemView.findViewById(R.id.item_view_button);
    }
  }
}