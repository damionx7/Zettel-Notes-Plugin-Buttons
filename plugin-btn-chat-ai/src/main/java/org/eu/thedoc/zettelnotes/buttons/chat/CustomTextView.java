package org.eu.thedoc.zettelnotes.buttons.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomTextView
    extends androidx.appcompat.widget.AppCompatTextView
    implements OnMenuItemClickListener {

  private static final String INSERT_TEXT = "Insert Text";
  private static final String REPLACE_TEXT = "Replace Text";
  private Listener mListener;

  public CustomTextView(
      @NonNull Context context) {
    super(context);
  }

  public CustomTextView(
      @NonNull Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomTextView(
      @NonNull Context context,
      @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onCreateContextMenu(ContextMenu menu) {
    if (mListener != null && (mListener.enableInsertMenuItem() || mListener.enableReplaceMenuItem())) {
      menu.add(Menu.NONE, R.id.menu_settings, 0, INSERT_TEXT).setOnMenuItemClickListener(this).setEnabled(mListener.enableInsertMenuItem());
      menu.add(Menu.NONE, R.id.menu_settings, 0, REPLACE_TEXT).setOnMenuItemClickListener(this).setEnabled(
          mListener.enableReplaceMenuItem());
    }
  }

  public void setListener(Listener listener) {
    mListener = listener;
  }

  @Override
  public boolean onMenuItemClick(
      @NonNull MenuItem item) {
    if (mListener != null) {
      String title = item.getTitle().toString();
      switch (title) {
        case INSERT_TEXT -> mListener.onMenuItemInsertClick("");
        case REPLACE_TEXT -> mListener.onMenuItemReplaceClick("");
      }
    }
    return false;
  }

  public interface Listener {

    void onMenuItemInsertClick(String text);

    void onMenuItemReplaceClick(String text);

    boolean enableInsertMenuItem();

    boolean enableReplaceMenuItem();
  }
}
