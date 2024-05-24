package org.eu.thedoc.zettelnotes.buttons.ocr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.googlecode.leptonica.android.AdaptiveMap;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Convert;
import com.googlecode.leptonica.android.Enhance;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Rotate;
import com.googlecode.leptonica.android.Skew;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.ProgressNotifier;
import com.googlecode.tesseract.android.TessBaseAPI.ProgressValues;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import org.eu.thedoc.zettelnotes.buttons.ocr.Tessaract.Constants;
import org.eu.thedoc.zettelnotes.plugins.base.utils.AppExecutor;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class MainActivity
    extends AppCompatActivity
    implements ProgressNotifier {

  private final Executor mHandler = AppExecutor.getInstance().mainThread();
  private final Executor mNetworkIO = AppExecutor.getInstance().networkIO();
  private final Executor mDiskIO = AppExecutor.getInstance().diskIO();

  private TessBaseAPI mAPI;
  private SharedPreferences mSharedPreferences;
  private boolean mSuccess;
  private File mDataDir;
  private String mLangCode;
  private int mPageSegmentationMode;
  private ProgressBar mProgressBar;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mProgressBar = findViewById(R.id.activity_main_progress_bar);

    //set directory
    File parent = new File(getFilesDir(), Constants.FOLDER);
    mDataDir = new File(parent, Constants.DATA_FOLDER);
    if (!mDataDir.exists() && mDataDir.mkdirs()) {
      //
    }

    //set preferences
    mSharedPreferences = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);

    mLangCode = mSharedPreferences.getString(getString(R.string.prefs_ocr_languages_key), Constants.DEFAULT_LANGUAGE);
    mPageSegmentationMode = Integer.parseInt(mSharedPreferences.getString(getString(R.string.prefs_ocr_page_segmentation_mode_key), "1"));

    //check if language data exists
    if (languageDataExists()) {
      //initialize ocr api
      mAPI = new TessBaseAPI(this);
      mSuccess = mAPI.init(parent.getAbsolutePath(), mLangCode);
      mAPI.setPageSegMode(mPageSegmentationMode);
    }
    //download data if corrupt of non existing
    if (!mSuccess) {
      //delete corrupt data file
      File dataFile = getDataFile();
      if (dataFile.exists() && dataFile.delete()) {
        //
      }
      ToastsHelper.showToast(this, "Language " + mLangCode + " data doesn't exist. Downloading...");
      Log.v(getPackageName(), "Language " + mLangCode + " data doesn't exist. Downloading...");
      downloadLanguageData();
    }

    //register listeners
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(new PickVisualMedia(), uri -> {
      if (uri != null) {
        Log.d(getPackageName(), "Selected URI: " + uri);
        try {
          //get bitmap
          InputStream is = getContentResolver().openInputStream(uri);
          Bitmap bitmap = BitmapFactory.decodeStream(is);
          //
          getTextFromBitmap(bitmap);
        } catch (Exception e) {
          ToastsHelper.showToast(this, e.toString());
          Log.e(getPackageName(), e.toString());
        }
      }
    });
    ActivityResultLauncher<Intent> clickImageIntentResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            try {
              Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
              //
              getTextFromBitmap(photo);
            } catch (Exception e) {
              showToast("Error " + e);
            }
          }
        });

    MaterialButton buttonCamera = findViewById(R.id.button_open_camera);
    buttonCamera.setOnClickListener(v -> {
      Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      clickImageIntentResultLauncher.launch(takePictureIntent);
    });

    MaterialButton buttonSelectImage = findViewById(R.id.button_select_image);
    buttonSelectImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
        .build()));
  }

  private boolean languageDataExists() {
    return getDataFile().exists();
  }

  private File getDataFile() {
    return new File(mDataDir, String.format(Constants.LANGUAGE_CODE, mLangCode));
  }

  public void getTextFromBitmap(final Bitmap bitmap) {
    //check if language data exist
    if (!languageDataExists()) {
      Log.v(getPackageName(), "Language " + mLangCode + " data doesn't exist. Downloading...");
      downloadLanguageData();
      return;
    }
    //check if mApi initialized
    if (!mSuccess) {
      //initialize ocr api
      mAPI = new TessBaseAPI(this);
      mSuccess = mAPI.init(mDataDir.getParentFile().getAbsolutePath(), mLangCode);
      mAPI.setPageSegMode(mPageSegmentationMode);
    }
    mDiskIO.execute(() -> {
      toggleProgressBar(true);
      //pre-process bitmap
      Bitmap processedBitmap = bitmap;
      if (mSharedPreferences.getBoolean(getString(R.string.prefs_ocr_pre_process_image_key), true)) {
        processedBitmap = preProcessBitmap(bitmap);
      }
      //
      mAPI.setImage(processedBitmap);

      try {
        String text = mAPI.getHOCRText(1);
        String clean_text = Html.fromHtml(text).toString().trim();

        if (text.isEmpty()) {
          showToast("Error : OCR Text is empty");
        } else {
          Log.v(getPackageName(), "confidence " + mAPI.meanConfidence());
          Log.v(getPackageName(), clean_text);
          showToast("Got text with confidence  " + mAPI.meanConfidence());
        }
      } catch (Exception e) {
        showToast("Error " + e);
        Log.e(getPackageName(), e.toString());
      } finally {
        //clear mApi
        mAPI.clear();
        //
        toggleProgressBar(false);
      }
    });
  }

  private void showToast(String message) {
    mHandler.execute(() -> ToastsHelper.showToast(MainActivity.this, message));
  }

  private void downloadLanguageData() {
    mNetworkIO.execute(() -> {
      String downloadURL = String.format(Constants.TESS_DATA_DOWNLOAD_URL, mLangCode);
      URL url, base, next;
      HttpURLConnection conn;
      String location;

      try {
        toggleProgressBar(true);
        while (true) {
          url = new URL(downloadURL);
          conn = (HttpURLConnection) url.openConnection();
          conn.setInstanceFollowRedirects(false);
          switch (conn.getResponseCode()) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
              location = conn.getHeaderField("Location");
              base = new URL(downloadURL);
              next = new URL(base, location);  // Deal with relative URLs
              downloadURL = next.toExternalForm();
              continue;
          }
          break;
        }
        conn.connect();

        int totalContentSize = conn.getContentLength();

        InputStream input = new BufferedInputStream(url.openStream());
        File destf = getDataFile();
        destf.createNewFile();
        OutputStream output = new FileOutputStream(destf);

        byte[] data = new byte[1024 * 6];
        int count, downloaded = 0;
        while ((count = input.read(data)) != -1) {
          output.write(data, 0, count);
          downloaded += count;
          int percentage = (downloaded * 100) / totalContentSize;
          Log.v(getPackageName(), percentage + "%");
        }
        output.flush();
        output.close();
        input.close();
        showToast("Download Successful!");
      } catch (Exception e) {
        showToast(e.toString());
        Log.e(getPackageName(), e.toString());
      } finally {
        toggleProgressBar(false);
      }
    });
  }

  private void toggleProgressBar(boolean toggle) {
    mHandler.execute(() -> mProgressBar.setVisibility(toggle ? View.VISIBLE : View.GONE));
  }

  public Bitmap preProcessBitmap(Bitmap bitmap) {
    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    Pix pix = ReadFile.readBitmap(bitmap);
    pix = Convert.convertTo8(pix);

    if (mSharedPreferences.getBoolean(getString(R.string.prefs_ocr_pre_process_image_enhance_contrast_key), true)) {
      pix = AdaptiveMap.pixContrastNorm(pix);
    }

    if (mSharedPreferences.getBoolean(getString(R.string.prefs_ocr_pre_process_image_unshare_masking_key), true)) {
      pix = Enhance.unsharpMasking(pix);
    }

    if (mSharedPreferences.getBoolean(getString(R.string.prefs_ocr_pre_process_image_otsu_threshold_key), true)) {
      pix = Binarize.otsuAdaptiveThreshold(pix);
    }

    if (mSharedPreferences.getBoolean(getString(R.string.prefs_ocr_pre_process_image_deskew_image_key), true)) {
      float f = Skew.findSkew(pix);
      pix = Rotate.rotate(pix, f);
    }

    return WriteFile.writeBitmap(pix);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(
      @NonNull MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_main_settings) {
      Intent intent = new Intent();
      intent.setClass(getApplicationContext(), SettingsActivity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onProgressValues(ProgressValues progressValues) {
    Log.v(getPackageName(), "onProgressValues" + progressValues.getPercent());
  }
}


