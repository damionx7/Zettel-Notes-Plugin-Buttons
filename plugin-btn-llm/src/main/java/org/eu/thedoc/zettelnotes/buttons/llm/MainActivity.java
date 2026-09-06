package org.eu.thedoc.zettelnotes.buttons.llm;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import java.io.File;
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
        modelPref.setEntries(new String[]{"Small (270M)", "Balanced (1B)", "Best (1.5B)"});
        modelPref.setEntryValues(new String[]{LlmModel.SMALL.name(), LlmModel.BALANCED.name(), LlmModel.LARGE.name()});
        modelPref.setOnPreferenceChangeListener((preference, newValue) -> {
          LlmModel selected = LlmModel.valueOf((String) newValue);
          if (ModelManager.isDownloaded(requireContext(), selected)) {
            return true;
          }
          downloadModelWithProgress(selected, (ListPreference) preference);
          return false;
        });
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

    private void downloadModelWithProgress(LlmModel model, ListPreference modelPref) {
      int padding = (int) (16 * getResources().getDisplayMetrics().density);

      TextView statusText = new TextView(requireContext());
      statusText.setText("Starting download\u2026");
      statusText.setPadding(padding, padding, padding, padding / 2);

      ProgressBar progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
      progressBar.setIndeterminate(false);
      progressBar.setMax(100);
      progressBar.setProgress(0);
      progressBar.setPadding(padding, 0, padding, padding);

      LinearLayout layout = new LinearLayout(requireContext());
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.addView(statusText);
      layout.addView(progressBar);

      AlertDialog dialog = new AlertDialog.Builder(requireContext())
          .setTitle("Downloading " + model.name())
          .setView(layout)
          .setCancelable(false)
          .create();
      dialog.show();

      ModelManager.download(requireContext(), model, new ModelManager.ProgressListener() {
        @Override
        public void onProgress(int percent) {
          if (!isAdded()) {
            return;
          }
          requireActivity().runOnUiThread(() -> {
            progressBar.setProgress(percent);
            statusText.setText("Downloading\u2026 " + percent + "%");
          });
        }

        @Override
        public void onComplete(File modelFile) {
          if (!isAdded()) {
            return;
          }
          requireActivity().runOnUiThread(() -> {
            dialog.dismiss();
            modelPref.setValue(model.name());
            Preference storagePref = findPreference("prefs_model_storage");
            if (storagePref != null) {
              storagePref.setSummary(buildStorageSummary());
            }
          });
        }

        @Override
        public void onError(Exception e) {
          if (!isAdded()) {
            return;
          }
          requireActivity().runOnUiThread(() -> {
            dialog.dismiss();
            new AlertDialog.Builder(requireContext())
                .setTitle("Download failed")
                .setMessage(e.getMessage() != null ? e.getMessage() : "Unknown error")
                .setPositiveButton("OK", null)
                .show();
          });
        }
      });
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