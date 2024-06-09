package org.eu.thedoc.zettelnotes.buttons.speech2text;

import android.content.Context;

public class LanguageModels {

  private static final String ARABIC = "vosk-model-ar-mgb2-0.4";
  private static final String CHINESE = "vosk-model-small-cn-0.22";
  private static final String CATALAN = "vosk-model-small-ca-0.4";
  private static final String DUTCH = "vosk-model-small-nl-0.22";
  private static final String ENGLISH = "vosk-model-small-en-us-0.15";
  private static final String ENGLISH_INDIA = "vosk-model-small-en-in-0.4";
  private static final String FRENCH = "vosk-model-small-fr-0.22";
  private static final String GERMAN = "vosk-model-small-de-0.15";
  private static final String HINDI = "vosk-model-small-hi-0.22";
  private static final String ITALIAN = "vosk-model-small-it-0.22";
  private static final String PERSIAN = "vosk-model-small-fa-0.4";
  private static final String PORTUGESE = "vosk-model-small-pt-0.3";
  private static final String POLISH = "vosk-model-small-pl-0.22";
  private static final String RUSSIAN = "vosk-model-small-ru-0.22";
  private static final String SPANISH = "vosk-model-small-es-0.42";
  private static final String TAGALOG = "vosk-model-tl-ph-generic-0.6";
  private static final String TURKISH = "vosk-model-small-tr-0.3";
  private static final String UKRAINIAN = "vosk-model-small-uk-v3-nano";
  private static final String VIETNAMESE = "vosk-model-small-vn-0.4";
  private static final String DOWNLOAD_URL = "https://alphacephei.com/vosk/models/";

  public static String getDownloadUrl(Context context, String langCode) {
    //https://alphacephei.com/vosk/models
    return String.format("%s%s.zip", DOWNLOAD_URL, getModel(context, langCode));
  }

  public static String getModel(Context context, String langCode) {
    String model = ENGLISH;
    if (langCode.equals(context.getString(R.string.pref_lang_code_arabic))) {
      model = ARABIC;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_chinese))) {
      model = CHINESE;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_catalan))) {
      model = CATALAN;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_dutch))) {
      model = DUTCH;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_english))) {
      model = ENGLISH;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_english_india))) {
      model = ENGLISH_INDIA;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_french))) {
      model = FRENCH;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_german))) {
      model = GERMAN;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_hindi))) {
      model = HINDI;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_italian))) {
      model = ITALIAN;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_persian))) {
      model = PERSIAN;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_portuguese))) {
      model = PORTUGESE;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_polish))) {
      model = POLISH;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_russian))) {
      model = RUSSIAN;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_spanish))) {
      model = SPANISH;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_tagalog))) {
      model = TAGALOG;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_turkish))) {
      model = TURKISH;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_ukrainian))) {
      model = UKRAINIAN;
    } else if (langCode.equals(context.getString(R.string.pref_lang_code_vietnamese))) {
      model = VIETNAMESE;
    }
    return model;
  }
}
