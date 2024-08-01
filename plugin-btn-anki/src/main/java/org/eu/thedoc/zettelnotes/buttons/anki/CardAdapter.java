package org.eu.thedoc.zettelnotes.buttons.anki;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.List;
import org.eu.thedoc.zettelnotes.buttons.anki.Parser.Card;

public class CardAdapter
    extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

  private final List<Card> mList;

  public CardAdapter(List<Card> list) {
    mList = list;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_card, parent, false));
  }

  @Override
  public void onBindViewHolder(
      @NonNull ViewHolder holder, int position) {
    Card card = mList.get(position);
    if (card != null) {
      holder.mQuestion.setText(card.question());
      holder.mAnswer.setText(card.answer());
      holder.mAnswer.setText(card.answer());
      holder.mType.setText(card.type());
    }
  }

  @Override
  public int getItemCount() {
    return mList.size();
  }

  public static class ViewHolder
      extends RecyclerView.ViewHolder {

    private final AppCompatTextView mQuestion, mAnswer;
    private final Chip mType;

    public ViewHolder(
        @NonNull View itemView) {
      super(itemView);

      mQuestion = itemView.findViewById(R.id.item_view_card_question);
      mAnswer = itemView.findViewById(R.id.item_view_card_answer);
      mType = itemView.findViewById(R.id.item_view_card_type);
      //Hide Type Chip During Production
      mType.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }
  }
}