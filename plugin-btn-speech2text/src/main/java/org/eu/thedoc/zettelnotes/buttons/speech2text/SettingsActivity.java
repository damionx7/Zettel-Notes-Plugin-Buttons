package org.eu.thedoc.zettelnotes.buttons.speech2text;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity
    extends AppCompatActivity {

  public static final String PREFS = "prefs";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, SettingsFragment.newInstance()).commit();
  }

  public static class SettingsFragment
      extends PreferenceFragmentCompat {

    private SharedPreferences mSharedPreferences;

    public static SettingsFragment newInstance() {
      return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      getPreferenceManager().setSharedPreferencesName(PREFS);
      mSharedPreferences = getActivity().getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);

      addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public boolean onPreferenceTreeClick(
        @NonNull Preference preference) {
      String key = preference.getKey();
      if (key.equals(getString(R.string.prefs_use_continuous_recognition))) {
        boolean value = mSharedPreferences.getBoolean(key, false);
        toggleComponents(value);
        return true;
      }
      return super.onPreferenceTreeClick(preference);
    }

    private void toggleComponents(boolean enableContinuousRecognizer) {
      Log.v(getClass().getName(), "toggleComponents " + enableContinuousRecognizer);

      ComponentName recognizerComponent = new ComponentName(getActivity(), RecognizerActivity.class);
      ComponentName continuousRecognizerComponent = new ComponentName(getActivity(), ContinuousRecognizerActivity.class);

      PackageManager packageManager = getActivity().getPackageManager();

      if (enableContinuousRecognizer) {
        packageManager.setComponentEnabledSetting(recognizerComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);

        packageManager.setComponentEnabledSetting(continuousRecognizerComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
      } else {
        packageManager.setComponentEnabledSetting(recognizerComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);

        packageManager.setComponentEnabledSetting(continuousRecognizerComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
      }

    }
  }
}


