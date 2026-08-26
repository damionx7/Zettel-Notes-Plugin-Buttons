package org.eu.thedoc.zettelnotes.buttons.barcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.File;
import java.io.FileOutputStream;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class GenerateActivity
    extends BaseActivity {

  public static final String INTENT_EXTRA_TEXT = "intent-extra-text";
  public static final String INTENT_EXTRA_URI = "intent-extra-uri";
  public static final String ERROR_STRING = "intent-error";
  private static final int MAX_QR_BYTES = 2953;
  private static final int SIZE = 1024;

  private Uri mGeneratedUri;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_generate);

    ImageView imageView = findViewById(R.id.barcodeImage);
    ProgressBar progressBar = findViewById(R.id.progressBar);
    Button copyButton = findViewById(R.id.copyToZettelButton);
    Button cancelButton = findViewById(R.id.cancelButton);

    String text = getIntent().getStringExtra(INTENT_EXTRA_TEXT);
    if (text == null || text.isEmpty()) {
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "No text selected to encode"));
      ToastsHelper.showToast(this, "Error: Select some text first");
      finish();
      return;
    }

    int byteLength = text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    if (byteLength > MAX_QR_BYTES) {
      setResult(RESULT_CANCELED,
          new Intent().putExtra(ERROR_STRING, "Selected text too long (" + byteLength + " bytes, max " + MAX_QR_BYTES + ")"));
      ToastsHelper.showToast(this, "Error: Selection too long for a scannable QR code (" + byteLength + "/" + MAX_QR_BYTES + " bytes)");
      finish();
      return;
    }

    progressBar.setVisibility(View.VISIBLE);

    try {
      Bitmap bitmap = generateQrBitmap(text);
      mGeneratedUri = saveBitmapAndGetUri(bitmap);
      imageView.setImageBitmap(bitmap);
      progressBar.setVisibility(View.GONE);
    } catch (Exception e) {
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, e.toString()));
      ToastsHelper.showToast(this, "Error: Could not generate barcode");
      finish();
      return;
    }

    copyButton.setOnClickListener(v -> {
      Intent resultIntent = new Intent().putExtra(INTENT_EXTRA_URI, mGeneratedUri);
      resultIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      String callingPackage = getCallingPackage();
      if (callingPackage != null) {
        grantUriPermission(callingPackage, mGeneratedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      setResult(RESULT_OK, resultIntent);
      finish();
    });

    cancelButton.setOnClickListener(v -> {
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "User cancelled"));
      finish();
    });
  }

  private Bitmap generateQrBitmap(String text) throws WriterException {
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, SIZE, SIZE);
    Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.RGB_565);
    for (int x = 0; x < SIZE; x++) {
      for (int y = 0; y < SIZE; y++) {
        bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
      }
    }
    return bitmap;
  }

  private Uri saveBitmapAndGetUri(Bitmap bitmap) throws Exception {
    File dir = new File(getCacheDir(), "barcodes");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File file = new File(dir, "qr_" + System.currentTimeMillis() + ".png");
    try (FileOutputStream out = new FileOutputStream(file)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }
    return FileProvider.getUriForFile(this, "org.eu.thedoc.zettelnotes.buttons.barcode.fileprovider", file);
  }
}