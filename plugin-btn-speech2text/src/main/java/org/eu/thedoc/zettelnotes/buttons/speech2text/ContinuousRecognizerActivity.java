package org.eu.thedoc.zettelnotes.buttons.speech2text;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eu.thedoc.zettelnotes.plugins.base.utils.AppExecutor;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

//https://github.com/alphacep/vosk-android-demo/blob/master/app/src/main/java/org/vosk/demo/VoskActivity.java
public class ContinuousRecognizerActivity
    extends AppCompatActivity
    implements RecognitionListener {

  private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
  private String mLangCode;
  private SpeechService mSpeechService;
  private AppCompatTextView mTextView;
  private MaterialButton mRecordButton, mStopButton;
  private SharedPreferences mSharedPreferences;
  private ExtendedFloatingActionButton mCopyToNoteButton;
  private String mResult;

  private void startRecording(boolean start) {
    if (start) {
      try {
        Model model = new Model(getModelFolder(mLangCode).getAbsolutePath());

        Recognizer rec = new Recognizer(model, 16000.0f);
        mSpeechService = new SpeechService(rec, 16000.0f);
        mSpeechService.startListening(this);
        mTextView.append("Listening (" + mLangCode + ")\n");

        toggleRecordState(true);
      } catch (IOException e) {
        Log.e(getClass().getName(), e.toString());
        showToast(e.toString());
        toggleRecordState(false);
      }
    } else {
      if (mSpeechService != null) {
        //stop
        mSpeechService.stop();
        mSpeechService = null;
        mTextView.append("Stopped ⏹");
        toggleRecordState(false);
      }
    }

  }

  private void toggleRecordState(boolean recording) {
    mRecordButton.setVisibility(recording ? View.GONE : View.VISIBLE);
    mStopButton.setVisibility(!recording ? View.GONE : View.VISIBLE);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mLangCode = mSharedPreferences.getString(getString(R.string.prefs_language_key), getString(R.string.pref_lang_code_english));
  }

  @Override
  protected void onStop() {
    super.onStop();
    mStopButton.callOnClick();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mSpeechService != null) {
      mSpeechService.stop();
      mSpeechService.shutdown();
    }
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mTextView = findViewById(R.id.activity_main_text_view);

    mRecordButton = findViewById(R.id.activity_main_button_record);
    mStopButton = findViewById(R.id.activity_main_button_stop);
    mStopButton.setVisibility(View.GONE);
    mCopyToNoteButton = findViewById(R.id.activity_main_extended_floating_action_button);

    mSharedPreferences = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
    mLangCode = mSharedPreferences.getString(getString(R.string.prefs_language_key), getString(R.string.pref_lang_code_english));

    mRecordButton.setOnClickListener(v -> initializeSpeechModel());
    mStopButton.setOnClickListener(v -> startRecording(false));
    mCopyToNoteButton.setOnClickListener(v -> {
      if (mResult != null && !mResult.isBlank()) {
        setResult(RESULT_OK, new Intent().putExtra(Button.RESULT_STRING, mResult));
        finish();
      } else {
        Log.e(getPackageName(), "Error Result is Null");

        setResult(RESULT_CANCELED, new Intent().putExtra(Button.ERROR_STRING, "Error Result is Null"));
        finish();
      }
    });
    //start with recording
    mRecordButton.callOnClick();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        initializeSpeechModel();
      } else {
        ToastsHelper.showToast(this, "Record Permission not Granted!");
      }
    }
  }

  private void addResult(String result) {
    AppExecutor.getInstance().mainThread().execute(() -> {
      try {
        if (!result.isBlank()) {
          String text = new JSONObject(result).getString("text");
          mResult = (mResult == null ? text : mResult + text) + "\n";
          mTextView.append(text);
          mTextView.append("\n");
        }
      } catch (Exception e) {
        //
      }
    });
  }

  @Override
  public void onPartialResult(String hypothesis) {
    //addResult(hypothesis);
  }

  @Override
  public void onResult(String hypothesis) {
    addResult(hypothesis);
  }

  @Override
  public void onFinalResult(String hypothesis) {
    addResult(hypothesis);
  }

  @Override
  public void onError(Exception exception) {
    Log.e(getClass().getName(), exception.toString());
    showToast(exception.toString());
  }

  @Override
  public void onTimeout() {
    Log.e(getClass().getName(), "onTimeout");
  }

  private void initializeSpeechModel() {
    LibVosk.setLogLevel(LogLevel.WARNINGS);

    // Check if user has given permission to record audio, init the model after permission is granted
    int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), permission.RECORD_AUDIO);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
      return;
    }
    mTextView.append("Recording permission. ✅\n");

    //Check if model exists
    File modelFolder = getModelFolder(mLangCode);
    if (!modelFolder.exists() || modelFolder.list() == null || modelFolder.list().length == 0) {
      downloadLanguageModel(mLangCode);
      return;
    }
    mTextView.append("Language model. ✅\n");

    //Start mic recognition
    startRecording(true);
  }

  private File getParentFolder() {
    File parentFolder = new File(getFilesDir(), "vosk-model");
    if (!parentFolder.exists() && parentFolder.mkdir()) {
      //
    }
    return parentFolder;
  }

  private File getModelFolder(String langCode) {
    return new File(getParentFolder(), LanguageModels.getModel(this, langCode));
  }

  private void downloadLanguageModel(String langCode) {
    final String downloadUrl = LanguageModels.getDownloadUrl(this, langCode);
    AppExecutor.getInstance().networkIO().execute(() -> {
      try {
        AppExecutor.getInstance().mainThread().execute(() -> mTextView.append("Downloading model for " + langCode + " language...\n"));
        //download model zip file
        URL url = new URL(downloadUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();

        File cacheFile = new File(getCacheDir(), langCode + ".zip");
        if (cacheFile.exists() && cacheFile.delete()) {
          //
        }
        try (InputStream inputStream = new BufferedInputStream(url.openStream()); FileOutputStream fileOutputStream = new FileOutputStream(
            cacheFile); BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 1024)) {

          byte[] data = new byte[1024];
          int count;
          while ((count = inputStream.read(data, 0, 1024)) != -1) {
            bufferedOutputStream.write(data, 0, count);
          }
          bufferedOutputStream.flush();
        }

        //extract downloaded zip file
        AppExecutor.getInstance().mainThread().execute(() -> mTextView.append("Extracting...\n"));
        File outputFolder = getParentFolder();
        try (FileInputStream fis = new FileInputStream(cacheFile); ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {

          ZipEntry ze;
          byte[] buffer = new byte[1024];
          while ((ze = zis.getNextEntry()) != null) {
            File outputFile = new File(outputFolder, ze.getName());

            // Create directories if necessary
            if (ze.isDirectory()) {
              outputFile.mkdirs();
              continue;
            } else {
              outputFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile); BufferedOutputStream bos = new BufferedOutputStream(fos,
                buffer.length)) {

              int len;
              while ((len = zis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
              }
              bos.flush();
            }
          }
        }
        Log.v(getClass().getName(), "model " + downloadUrl + " downloaded.");

        AppExecutor.getInstance().mainThread().execute(this::initializeSpeechModel);
      } catch (Exception e) {
        Log.e(getClass().getName(), e.toString());
        showToast(e.toString());
      }
    });
  }

  private void showToast(String text) {
    AppExecutor.getInstance().mainThread().execute(() -> ToastsHelper.showToast(ContinuousRecognizerActivity.this, text));
  }

}