package org.eu.thedoc.zettelnotes.buttons.imgbb;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity
    extends BaseActivity {

  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  public static final String ERROR_STRING = "intent-error";

  private static final long MAX_UPLOAD_BYTES = 32L * 1024L * 1024L; // ImgBB's stated limit

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private ProgressBar progress;
  private TextView status;
  private ActivityResultLauncher<String> picker;
  private Api api;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    int p = dp(20);
    root.setPadding(p, p, p, p);

    TextView title = new TextView(this);
    title.setText("Upload image to ImgBB");
    title.setTextSize(20);
    root.addView(title);

    status = new TextView(this);
    status.setText("\nChoose an image from your device.");
    root.addView(status);

    Button choose = new Button(this);
    choose.setText("Choose image");
    root.addView(choose);

    progress = new ProgressBar(this);
    progress.setVisibility(ProgressBar.GONE);
    root.addView(progress);

    setContentView(root);

    api = new Retrofit.Builder()
        .baseUrl("https://api.imgbb.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Api.class);

    picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
      if (uri != null) {
        upload(uri);
      } else {
        finishWithError("No image selected");
      }
    });

    choose.setOnClickListener(v -> picker.launch("image/*"));
  }

  private void upload(Uri uri) {
    String key = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE).getString(SettingsActivity.PREF_API_KEY, "");

    if (key == null || key
        .trim()
        .isEmpty()) {
      Toast
          .makeText(this, "Set the ImgBB API key first", Toast.LENGTH_LONG)
          .show();
      startActivity(new Intent(this, SettingsActivity.class));
      finish();
      return;
    }

    setLoading(true);

    executor.execute(() -> {
      File temp = null;
      try {
        String filename = getFileName(uri);
        if (filename == null || filename
            .trim()
            .isEmpty()) {
          filename = "image";
        }

        String mime = getContentResolver().getType(uri);
        if (mime == null) {
          mime = URLConnection.guessContentTypeFromName(filename);
        }
        if (mime == null) {
          mime = "application/octet-stream";
        }

        temp = new File(getCacheDir(), "imgbb_" + System.currentTimeMillis());
        try (InputStream in = getContentResolver().openInputStream(uri); FileOutputStream out = new FileOutputStream(temp)) {

          if (in == null) {
            throw new IllegalStateException("Cannot read selected image");
          }

          byte[] buffer = new byte[8192];
          int n;
          long total = 0;
          while ((n = in.read(buffer)) != -1) {
            total += n;
            if (total > MAX_UPLOAD_BYTES) {
              throw new IllegalArgumentException("ImgBB allows images up to 32 MB");
            }
            out.write(buffer, 0, n);
          }
        }

        RequestBody body = RequestBody.create(MediaType.parse(mime), temp);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", filename, body);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), filename);

        File finalTemp = temp;

        api
            .upload(key.trim(), part, name)
            .enqueue(new Callback<ApiResponse>() {
              @Override
              public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (finalTemp.exists()) {
                  finalTemp.delete();
                }

                ApiResponse body = response.body();
                if (!response.isSuccessful() || body == null || !body.success || body.data == null || body.data.url == null) {
                  finishWithError(extractErrorMessage(response));
                  return;
                }

                String markdown = "![" + body.data.displayUrl + "](" + body.data.url + ")";

                setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, markdown));
                finish();
              }

              @Override
              public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (finalTemp.exists()) {
                  finalTemp.delete();
                }
                finishWithError("Upload failed: " + (t.getMessage() == null ? "network error" : t.getMessage()));
              }
            });
      } catch (Exception e) {
        if (temp != null && temp.exists()) {
          temp.delete();
        }
        finishWithError(e.getMessage() == null ? "Unable to read image" : e.getMessage());
      }
    });
  }

  private String extractErrorMessage(Response<ApiResponse> response) {
    ApiResponse body = response.body();
    if (body != null && body.error != null && body.error.message != null && !body.error.message.isEmpty()) {
      return "ImgBB upload failed: " + body.error.message;
    }
    return "ImgBB upload failed (" + response.code() + ")";
  }

  private void setLoading(boolean loading) {
    progress.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE);
    status.setText(loading ? "Uploading..." : "Choose an image from your device.");
  }

  private void finishWithError(String message) {
    runOnUiThread(() -> {
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, message));
      Toast
          .makeText(this, message, Toast.LENGTH_LONG)
          .show();
      finish();
    });
  }

  private String getFileName(Uri uri) {
    Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);

    try (cursor) {
      if (cursor == null) {
        return null;
      }
      int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      return index >= 0 && cursor.moveToFirst() ? cursor.getString(index) : null;
    }
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    executor.shutdown();
  }
}


