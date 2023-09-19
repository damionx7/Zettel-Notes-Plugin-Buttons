package org.eu.thedoc.zettelnotes.buttons.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager.OnPreferenceTreeClickListener;

public class SettingsActivity
    extends AppCompatActivity {

  public static final String PREFS = "_prefs";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, SettingsFragment.newInstance()).commit();
  }

  public static class SettingsFragment
      extends PreferenceFragmentCompat
      implements OnPreferenceTreeClickListener {

    public static SettingsFragment newInstance() {
      return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      getPreferenceManager().setSharedPreferencesName(PREFS);
      addPreferencesFromResource(R.xml.prefs);
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
  }
}


