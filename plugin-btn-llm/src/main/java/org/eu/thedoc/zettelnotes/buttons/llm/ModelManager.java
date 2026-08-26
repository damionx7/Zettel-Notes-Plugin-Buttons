package org.eu.thedoc.zettelnotes.buttons.llm;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ModelManager {

  private static final OkHttpClient CLIENT = new OkHttpClient();

  public static File getModelFile(Context context, LlmModel model) {
    return new File(context.getFilesDir(), model.fileName);
  }

  public static boolean isDownloaded(Context context, LlmModel model) {
    File file = getModelFile(context, model);
    return file.exists() && file.length() > 0;
  }

  public static void download(Context context, LlmModel model, ProgressListener listener) {
    new Thread(() -> {
      File target = getModelFile(context, model);
      File tmp = new File(context.getFilesDir(), model.fileName + ".tmp");
      Request request = new Request.Builder()
          .url(model.downloadUrl)
          .build();

      try (Response response = CLIENT
          .newCall(request)
          .execute()) {
        if (!response.isSuccessful() || response.body() == null) {
          listener.onError(new Exception("Download failed: HTTP " + response.code()));
          return;
        }

        long contentLength = response
            .body()
            .contentLength();
        if (contentLength <= 0) {
          contentLength = model.approxBytes;
        }

        try (InputStream in = response
            .body()
            .byteStream(); FileOutputStream out = new FileOutputStream(tmp)) {
          byte[] buffer = new byte[8192];
          long downloaded = 0;
          int read;
          int lastPercent = -1;
          while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            downloaded += read;
            int percent = (int) ((downloaded * 100) / contentLength);
            if (percent != lastPercent) {
              lastPercent = percent;
              listener.onProgress(percent);
            }
          }
        }

        if (!tmp.renameTo(target)) {
          listener.onError(new Exception("Could not finalize model file"));
          return;
        }
        listener.onComplete(target);

      } catch (Exception e) {
        listener.onError(e);
      }
    }).start();
  }

  public interface ProgressListener {

    void onProgress(int percent);

    void onComplete(File modelFile);

    void onError(Exception e);
  }
}