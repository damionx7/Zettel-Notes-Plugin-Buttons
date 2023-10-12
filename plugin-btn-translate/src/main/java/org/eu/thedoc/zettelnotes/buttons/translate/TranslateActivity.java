package org.eu.thedoc.zettelnotes.buttons.translate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class TranslateActivity
    extends AppCompatActivity
    implements OnFailureListener {

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  public static final String INTENT_EXTRA_USE_LAST_SELECTED = "intent-extra-last-selected";
  public static final String ERROR_STRING = "intent-error";

  public static final String PREFS_LAST_SELECTED = "prefs-last-selected";
  public static final String PREFS_LAST_SELECTED_CODE = "prefs-last-selected-code";
  public static final String PREFS = "_settings";

  private static final String IDENTIFY_LANGUAGE_ERROR = "Can't Identify Language. Try selecting larger text for translation";

  private Translator mTranslator;
  private SharedPreferences mPrefs;

  private ProgressBar mProgressBar;
  private EditText etTranslate, etTranslation;
  private Spinner sSource, sTarget;
  private Button mButtonSubmit, mButtonTranslate;

  private String sourceLanguageCode, targetLanguageCode;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);
    mPrefs = getSharedPreferences(PREFS, MODE_PRIVATE);

    mProgressBar = findViewById(R.id.progressBar);
    mProgressBar.setIndeterminate(true);

    etTranslate = findViewById(R.id.etTranslate);
    etTranslation = findViewById(R.id.etTranslation);
    sSource = findViewById(R.id.sSource);
    sTarget = findViewById(R.id.sTarget);
    mButtonSubmit = findViewById(R.id.btnSubmit);
    mButtonTranslate = findViewById(R.id.btnTranslate);

    String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    if (txtSelected == null || txtSelected.isEmpty()) {
      ToastsHelper.showToast(this, "Select some text");
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Select some text"));
      finish();
    } else {
      etTranslate.setText(txtSelected);

      //get supported language codes
      final HashMap<String, String> languageHashMap = populateLanguageCodes(false);
      final HashMap<String, String> codeFirstHashMap = populateLanguageCodes(true);

      //show as sorted list
      List<String> supportedLanguages = new ArrayList<>(languageHashMap.keySet());
      Collections.sort(supportedLanguages, String.CASE_INSENSITIVE_ORDER);

      ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, supportedLanguages);
      sSource.setAdapter(sourceAdapter);
      sSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
          String language = supportedLanguages.get(position);
          sourceLanguageCode = languageHashMap.get(language);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
          //
        }
      });
      ArrayAdapter<String> targetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, supportedLanguages);
      sTarget.setAdapter(targetAdapter);
      sTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
          String language = supportedLanguages.get(position);
          targetLanguageCode = languageHashMap.get(language);

          putString(PREFS_LAST_SELECTED, language);
          putString(PREFS_LAST_SELECTED_CODE, targetLanguageCode);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
          //
        }
      });

      mButtonTranslate.setOnClickListener(view -> translate());
      mButtonSubmit.setOnClickListener(view -> {
        String translation = etTranslation.getText().toString();
        setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, translation));
        finish();
      });

      //identify language and set source language code on start
      LanguageIdentifier identifier = LanguageIdentification.getClient();
      identifier.identifyLanguage(txtSelected).addOnSuccessListener(languageCode -> {
        if (languageCode.equals("und")) {
          //set fallback language as english
          languageCode = "en";
          onFailure(new Exception(IDENTIFY_LANGUAGE_ERROR));
        }
        sourceLanguageCode = TranslateLanguage.fromLanguageTag(languageCode);
        String sourceLanguage = codeFirstHashMap.get(sourceLanguageCode);
        int index = supportedLanguages.indexOf(sourceLanguage);
        sSource.setSelection(index);

        //default language
        String lastLanguage = mPrefs.getString(PREFS_LAST_SELECTED, "SPANISH");
        String lastLanguageCode = mPrefs.getString(PREFS_LAST_SELECTED_CODE, TranslateLanguage.SPANISH);

        sTarget.setSelection(supportedLanguages.indexOf(lastLanguage));
        targetLanguageCode = TranslateLanguage.fromLanguageTag(lastLanguageCode);

        mButtonTranslate.callOnClick();
      }).addOnFailureListener(this);
    }
  }

  @Override
  public void onFailure(
      @NonNull Exception e) {
    mProgressBar.setVisibility(View.GONE);
    ToastsHelper.showToast(this, e.toString());
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mProgressBar != null) {
      mProgressBar.setVisibility(View.GONE);
    }
    if (mTranslator != null) {
      mTranslator.close();
    }
  }

  private void translate() {
    mProgressBar.setVisibility(View.VISIBLE);

    String txtToTranslate = etTranslate.getText().toString();
    if (sourceLanguageCode != null && targetLanguageCode != null) {
      TranslatorOptions options = new TranslatorOptions.Builder().setSourceLanguage(sourceLanguageCode).setTargetLanguage(
          targetLanguageCode).build();
      mTranslator = Translation.getClient(options);
      getLifecycle().addObserver(mTranslator);
      mTranslator.downloadModelIfNeeded().addOnSuccessListener(unused -> mTranslator.translate(txtToTranslate).addOnSuccessListener(s -> {
        etTranslation.setText(s);
        mProgressBar.setVisibility(View.GONE);
      }).addOnFailureListener(this)).addOnFailureListener(this);
    } else {
      onFailure(new Exception(IDENTIFY_LANGUAGE_ERROR));
    }
  }

  //HashMap<Language, Code>
  private HashMap<String, String> populateLanguageCodes(boolean codeFirst) {
    HashMap<String, String> hashMap = new HashMap<>();
    Field[] fields = TranslateLanguage.class.getFields();
    for (Field f : fields) {
      if (Modifier.isStatic(f.getModifiers())) {
        Class<?> t = f.getType();
        if (t == String.class) {
          try {
            String fieldName = f.getName();
            String fieldValue = (String) f.get(null);
            if (codeFirst) {
              hashMap.put(fieldValue, fieldName);
            } else {
              hashMap.put(fieldName, fieldValue);
            }
          } catch (IllegalAccessException e) {
            onFailure(e);
          }
        }
      }
    }
    return hashMap;
  }

  public void putString(String key, String value) {
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString(key, value);
    editor.apply();
  }

}
