package org.eu.thedoc.zettelnotes.buttons.location;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button extends ButtonInterface {

  public static final String INTENT_ACTION_LOC = "org.eu.thedoc.zettelnotes.intent.buttons.location";

  private final Listener mListener = new Listener() {
    @Override
    public void onClick () {
      try {
        if (mFragmentManager != null) {
          FragmentTransaction ft = mFragmentManager.beginTransaction();
          MyFragment fragment = new MyFragment();
          fragment.bindCallback(mCallback);
          ft.add(fragment, "");
          if (mFragmentManager.isStateSaved()) {
            ft.commitAllowingStateLoss();
          } else {
            ft.commit();
          }
        }
      } catch (Exception e) {
        Log.e("LocationButton", "Exception thrown in Fragment Transaction");
      }
    }

    @Override
    public boolean onLongClick () {
      return false;
    }
  };

  public Button () {}

  @Override
  public String getName () {
    return "Location";
  }

  @Override
  public Listener getListener () {
    return mListener;
  }

  public static class MyFragment extends Fragment {
    private ActivityResultLauncher<Intent> locationResultLauncher;
    private Callback mCallback;

    @Override
    public void onCreate (Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      locationResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
          String location = result.getData().getStringExtra(LocationActivity.INTENT_EXTRA_LOCATION);
          if (mCallback != null && location != null && !location.isEmpty()){
            mCallback.insertText(location);
          } else showToast("Try again");
        } else {
          if (result.getData() != null) {
            String error = result.getData().getStringExtra(LocationActivity.ERROR_STRING);
            showToast("Error: " + error);
          }
        }
      });
      try {
        locationResultLauncher.launch(new Intent(INTENT_ACTION_LOC));
      } catch (ActivityNotFoundException e) {
        showToast(e.toString());
      }
    }

    public void bindCallback (Callback callback) {
      mCallback = callback;
    }

    public void showToast (String text) {
      if (!text.isEmpty()) Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
  }


}
