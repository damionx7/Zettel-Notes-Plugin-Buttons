package org.eu.thedoc.zettelnotes.buttons.translate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TranslateActivity extends AppCompatActivity implements OnFailureListener {

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  public static final String INTENT_EXTRA_USE_LAST_SELECTED = "intent-extra-last-selected";
  public static final String ERROR_STRING = "intent-error";

  public static final String PREFS_LAST_SELECTED = "prefs-last-selected";
  public static final String PREFS_LAST_SELECTED_CODE = "prefs-last-selected-code";
  public static final String PREFS = "_settings";

  private ProgressBar mProgressBar;
  private Translator mTranslator;

  @Override
  protected void onCreate (@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);
    mProgressBar = findViewById(R.id.progressBar);
    mProgressBar.setIndeterminate(true);
    SharedPreferences sharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

    String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    if (txtSelected == null || txtSelected.isEmpty()) {
      showToast("Select some text");
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Select some text"));
      finish();
    } else {
      mProgressBar.setVisibility(View.VISIBLE);

      //get supported language codes
      final HashMap<String, String> languageCodes = populateLanguageCodes();
      //show as sorted list
      List<String> supportedLanguages = new ArrayList<>(languageCodes.keySet());
      Collections.sort(supportedLanguages, String.CASE_INSENSITIVE_ORDER);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setItems(supportedLanguages.toArray(new String[0]), (dialogInterface, which) -> {
        //get language from hashMap
        String language = supportedLanguages.get(which);
        String code = languageCodes.get(language);
        showToast("Translating to " + language + ":" + code);

        //save to prefs
        sharedPreferences.getString(PREFS_LAST_SELECTED, language);
        sharedPreferences.getString(PREFS_LAST_SELECTED_CODE, code);
        //translate
        LanguageIdentifier identifier = LanguageIdentification.getClient();
        identifier.identifyLanguage(txtSelected).addOnSuccessListener(languageCode -> {
          if (languageCode.equals("und")) onFailure(new Exception("Can't identify language"));
          else {
            String sourceLang = TranslateLanguage.fromLanguageTag(languageCode);
            if (sourceLang != null) {
              TranslatorOptions options = new TranslatorOptions.Builder().setSourceLanguage(sourceLang).setTargetLanguage(code).build();
              mTranslator = Translation.getClient(options);
              getLifecycle().addObserver(mTranslator);
              mTranslator.downloadModelIfNeeded().addOnSuccessListener(unused -> mTranslator.translate(txtSelected).addOnSuccessListener(s -> {
                setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, s));
                finish();
              }).addOnFailureListener(this)).addOnFailureListener(this);
            } else onFailure(new Exception("Can't identify language"));
          }
        }).addOnFailureListener(this);
      });
      builder.setOnCancelListener(dialog -> finish());
      builder.show();
    }
  }

  @Override
  public void onFailure (@NonNull Exception e) {
    showToast(e.toString());
    setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, e.toString()));
    finish();
  }

  @Override
  protected void onStop () {
    super.onStop();
    if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
    if (mTranslator != null) mTranslator.close();
  }

  private HashMap<String, String> populateLanguageCodes () {
    HashMap<String, String> hashMap = new HashMap<>();
    Field[] fields = TranslateLanguage.class.getFields();
    for (Field f : fields) {
      if (Modifier.isStatic(f.getModifiers())) {
        Class<?> t = f.getType();
        if (t == String.class) {
          try {
            String fieldName = f.getName();
            String fieldValue = (String) f.get(null);
            hashMap.put(fieldName, fieldValue);
          } catch (IllegalAccessException e) {
            onFailure(e);
          }
        }
      }
    }
    return hashMap;
  }

  private void showToast (String text) {
    if (!text.isEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

}
