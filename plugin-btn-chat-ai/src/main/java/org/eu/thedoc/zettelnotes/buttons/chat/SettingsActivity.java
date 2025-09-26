package org.eu.thedoc.zettelnotes.buttons.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager.OnPreferenceTreeClickListener;

public class SettingsActivity
    extends BaseActivity {

  public static final String PREFS = "_prefs";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, SettingsFragment.newInstance()).commit();
  }

  public static class SettingsFragment
      extends PreferenceFragmentCompat
      implements OnPreferenceTreeClickListener, OnSharedPreferenceChangeListener {

    private SharedPreferences mSharedPreferences;
    private Preference mCustomModelPreference;

    public static SettingsFragment newInstance() {
      return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      getPreferenceManager().setSharedPreferencesName(PREFS);
      addPreferencesFromResource(R.xml.prefs);
      mCustomModelPreference = getPreferenceManager().findPreference(getString(R.string.prefs_custom_model_key));

      mSharedPreferences = getPreferenceManager().getSharedPreferences();

      String model = mSharedPreferences.getString(getString(R.string.prefs_api_model_key), getString(R.string.model_gpt_5));
      mCustomModelPreference.setEnabled(model.equals(getString(R.string.model_custom)));
    }


    @Override
    public boolean onPreferenceTreeClick(
        @NonNull Preference preference) {
      String key = preference.getKey();
      if (key.equals(getString(R.string.prefs_register_key))) {
        try {
          Uri uri = Uri.parse("https://platform.openai.com/account/api-keys");
          startActivity(new Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
          Log.e("SETTINGS", e.toString());
        }
        return true;
      }
      return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onResume() {
      super.onResume();
      getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
      super.onPause();
      getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        @Nullable String key) {
      if (key == null) {
        return;
      }

      if (key.equals(getString(R.string.prefs_api_model_key))) {
        String model = mSharedPreferences.getString(key, getString(R.string.model_gpt_5));
        mCustomModelPreference.setEnabled(model.equals(getString(R.string.model_custom)));
      }
    }
  }
}