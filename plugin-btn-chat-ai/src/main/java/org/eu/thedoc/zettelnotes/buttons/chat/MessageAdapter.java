package org.eu.thedoc.zettelnotes.buttons.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatUser;
import java.util.ArrayList;
import java.util.List;
import org.eu.thedoc.zettelnotes.buttons.chat.CustomTextView.Listener;

public class MessageAdapter
    extends BaseAdapter
    implements Listener {

  private static final int LOCAL = 0;
  private static final int REMOTE = 1;
  private static final int INVALID = -1;

  private final List<ChatMessage> mList = new ArrayList<>();
  private Listener mListener;

  public void setListener(Listener listener) {
    mListener = listener;
  }

  @Override
  public int getItemViewType(int position) {
    if (mList.get(position).getRole().equals(ChatUser.ASSISTANT)) {
      return REMOTE;
    } else if (mList.get(position).getRole().equals(ChatUser.USER)) {
      return LOCAL;
    }
    return INVALID;
  }

  public int addItem(ChatMessage message) {
    mList.add(message);
    notifyDataSetChanged();
    return mList.size() - 1;
  }


  public void updateItem(int pos, ChatMessage chatMessage) {
    ChatMessage message = getItem(pos);
    message.setContent(chatMessage.getContent());
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mList.size();
  }

  public ChatMessage getItem(int position) {
    return mList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    MessageViewHolder holder = null;
    if (convertView == null) {
      int type = getItemViewType(position);
      switch (type) {
        case LOCAL -> {
          convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_local, null);
          holder = new MessageViewHolder(convertView, this);
        }
        case REMOTE -> {
          convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_remote, null);
          holder = new MessageViewHolder(convertView, this);
        }
      }
      convertView.setTag(holder);
    } else {
      holder = (MessageViewHolder) convertView.getTag();
    }
    holder.bindTo(getItem(position));
    return convertView;
  }

  @Override
  public void onMenuItemInsertClick(String text) {
    if (mListener != null) {
      mListener.onMenuItemInsertClick(text);
    }
  }

  @Override
  public void onMenuItemReplaceClick(String text) {
    if (mListener != null) {
      mListener.onMenuItemReplaceClick(text);
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
