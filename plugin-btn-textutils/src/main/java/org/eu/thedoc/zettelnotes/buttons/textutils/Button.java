package org.eu.thedoc.zettelnotes.buttons.textutils;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentTransaction;

import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button extends ButtonInterface {

  private final Listener mListener = new Listener() {
    @Override
    public void onClick () {
      try {
        if (mFragmentManager != null) {
          FragmentTransaction ft = mFragmentManager.beginTransaction();
          MyFragment fragment = new MyFragment();
          fragment.bindCallback(mCallback);
          fragment.show(ft, "");
        }
      } catch (Exception e) {
        Log.e("TextUtilsButton", "Exception thrown in Fragment Transaction");
      }
    }

    @Override
    public boolean onLongClick () {
      return false;
    }
  };

  @Override
  public String getName () {
    return "TextUtils";
  }

  @Override
  public Listener getListener () {
    return mListener;
  }

  public static class MyFragment extends AppCompatDialogFragment {
    private Callback mCallback;

    @NonNull
    @Override
    public Dialog onCreateDialog (@Nullable Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      String[] formatOptions = {"Remove newlines", "Split to newlines"};
      builder.setItems(formatOptions, (dialog, which) -> {
        if (mCallback != null) {
          String txtSelected = mCallback.getTextSelected();
          if (txtSelected.isEmpty()) {
            showToast("Select some text");
          } else {
            switch (which) {
              case 0:
                mCallback.replaceTextSelected(removeNewLines(txtSelected));
                showToast("Done");
                break;
              case 1:
                mCallback.replaceTextSelected(addNewLines(txtSelected));
                showToast("Done");
                break;
            }
          }
        }
      });
      return builder.create();
    }

    public void bindCallback (Callback callback) {
      mCallback = callback;
    }

    private void showToast (String text) {
      if (!text.isEmpty()) Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private String removeNewLines (String selectedText){
      return selectedText.replaceAll("\\n+", " ");
    }

    private String addNewLines (String selectedText){
      return selectedText.replaceAll("\\s+", "\n");
    }
  }

}
