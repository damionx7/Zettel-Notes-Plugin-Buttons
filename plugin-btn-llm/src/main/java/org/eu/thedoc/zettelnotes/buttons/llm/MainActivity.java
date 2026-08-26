package org.eu.thedoc.zettelnotes.buttons.llm;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;

public class MainActivity
    extends BaseActivity {

  public static final String PREFS = "prefs";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, SettingsFragment.newInstance())
        .commit();
  }

  public static class SettingsFragment
      extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance() {
      return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      getPreferenceManager().setSharedPreferencesName(PREFS);
      addPreferencesFromResource(R.xml.preferences);

      ListPreference modelPref = findPreference("prefs_model");
      if (modelPref != null) {
        modelPref.setEntries(new String[]{"Small (270M, ~250MB)", "Balanced (1B, ~800MB)", "Best (2B, ~1.4GB)"});
        modelPref.setEntryValues(new String[]{LlmModel.QWEN_0_5B.name(), LlmModel.QWEN_1_5B.name(), LlmModel.QWEN_1_5B_4K.name()});
      }

      Preference storagePref = findPreference("prefs_model_storage");
      if (storagePref != null) {
        storagePref.setSummary(buildStorageSummary());
        storagePref.setOnPreferenceClickListener(pref -> {
          showModelManagerDialog();
          return true;
        });
      }
    }

    private String buildStorageSummary() {
      long totalBytes = 0;
      int count = 0;
      for (LlmModel model : LlmModel.values()) {
        if (ModelManager.isDownloaded(requireContext(), model)) {
          totalBytes += ModelManager
              .getModelFile(requireContext(), model)
              .length();
          count++;
        }
      }
      if (count == 0) {
        return "No models downloaded";
      }
      return count + " model(s), " + (totalBytes / 1024 / 1024) + " MB used";
    }

    private void showModelManagerDialog() {
      java.util.List<LlmModel> downloaded = new java.util.ArrayList<>();
      for (LlmModel model : LlmModel.values()) {
        if (ModelManager.isDownloaded(requireContext(), model)) {
          downloaded.add(model);
        }
      }

      if (downloaded.isEmpty()) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Downloaded models")
            .setMessage("No models downloaded yet. A model downloads automatically the first time you use an AI action.")
            .setPositiveButton("OK", null)
            .show();
        return;
      }

      String[] labels = new String[downloaded.size()];
      for (int i = 0; i < downloaded.size(); i++) {
        LlmModel m = downloaded.get(i);
        long sizeMb = ModelManager
            .getModelFile(requireContext(), m)
            .length() / 1024 / 1024;
        labels[i] = m.name() + " (" + sizeMb + " MB) — tap to delete";
      }

      new AlertDialog.Builder(requireContext())
          .setTitle("Downloaded models")
          .setItems(labels, (dialog, which) -> {
            LlmModel toDelete = downloaded.get(which);
            new AlertDialog.Builder(requireContext())
                .setTitle("Delete " + toDelete.name() + "?")
                .setMessage("You'll need to re-download it to use this model again.")
                .setPositiveButton("Delete", (d, w) -> {
                  ModelManager
                      .getModelFile(requireContext(), toDelete)
                      .delete();
                  Preference storagePref = findPreference("prefs_model_storage");
                  if (storagePref != null) {
                    storagePref.setSummary(buildStorageSummary());
                  }
                })
                .setNegativeButton("Cancel", null)
                .show();
          })
          .show();
    }
  }
}