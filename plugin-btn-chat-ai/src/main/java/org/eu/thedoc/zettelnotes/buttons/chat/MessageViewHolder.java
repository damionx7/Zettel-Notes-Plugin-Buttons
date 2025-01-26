package org.eu.thedoc.zettelnotes.buttons.chat;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.cjcrafter.openai.chat.ChatMessage;
import io.noties.markwon.Markwon;
import org.eu.thedoc.zettelnotes.buttons.chat.CustomTextView.Listener;

public class MessageViewHolder
    extends RecyclerView.ViewHolder
    implements Listener {

  protected final Context mContext;
  private final CardView mCardView;
  private final CustomTextView mChatView;
  private final Listener mListener;
  private ChatMessage mChatMessage;
  private Markwon mMarkwon;

  public MessageViewHolder(
      @NonNull View itemView, CustomTextView.Listener listener) {
    super(itemView);
    mContext = itemView.getContext();
    mCardView = itemView.findViewById(R.id.card_view);
    mChatView = itemView.findViewById(R.id.tv_chat);
    mChatView.setListener(this);
    mListener = listener;
    mMarkwon = Markwon.create(itemView.getContext());
  }

  public void bindTo(
      @Nullable ChatMessage item) {
    if (item != null) {
      mChatMessage = item;

      if (item.getContent() != null) {
        mMarkwon.setMarkdown(mChatView, item.getContent());
      }
      mChatView.setSelectAllOnFocus(true);
    }
  }

  @Override
  public void onMenuItemInsertClick(String text) {
    if (mListener != null && mChatMessage != null) {
      mListener.onMenuItemInsertClick(mChatMessage.getContent());
    }
  }

  @Override
  public void onMenuItemReplaceClick(String text) {
    if (mListener != null && mChatMessage != null) {
      mListener.onMenuItemReplaceClick(mChatMessage.getContent());
    }
  }

  @Override
  public boolean enableInsertMenuItem() {
    if (mListener != null) {
      return mListener.enableInsertMenuItem();
    }
    return false;
  }

  @Override
  public boolean enableReplaceMenuItem() {
    if (mListener != null) {
      return mListener.enableReplaceMenuItem();
    }
    return false;
  }
}