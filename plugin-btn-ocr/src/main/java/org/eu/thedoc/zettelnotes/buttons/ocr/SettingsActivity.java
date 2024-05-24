package org.eu.thedoc.zettelnotes.buttons.ocr;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    public static SettingsFragment newInstance() {
      return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      getPreferenceManager().setSharedPreferencesName(PREFS);
      addPreferencesFromResource(R.xml.prefs);
    }
  }
}


