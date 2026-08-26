package org.eu.thedoc.zettelnotes.buttons.barcode;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class ScanActivity
    extends BaseActivity {

  public static final String INTENT_EXTRA_RESULT = "intent-extra-result";
  public static final String ERROR_STRING = "intent-error";

  private ActivityResultLauncher<ScanOptions> mBarcodeLauncher;
  private String mScannedText;
  private boolean mLaunchedForResult;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);

    mLaunchedForResult = getCallingPackage() != null;

    mBarcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
      if (result.getContents() == null) {
        if (mLaunchedForResult) {
          setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Scan cancelled"));
        }
        finish();
      } else {
        mScannedText = result.getContents();
        showResult(mScannedText, result.getFormatName());
      }
    });

    launchScanner();
  }

  private void launchScanner() {
    ScanOptions options = new ScanOptions();
    options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
    options.setPrompt("Scan a barcode or QR code");
    options.setBeepEnabled(true);
    options.setOrientationLocked(true);
    options.setCaptureActivity(CaptureActivityPortrait.class);
    mBarcodeLauncher.launch(options);
  }

  private void showResult(String text, String formatName) {

    TextView formatLabel = findViewById(R.id.scannedFormatLabel);
    TextView textView = findViewById(R.id.scannedTextView);
    Button actionButton = findViewById(R.id.copyToZettelButton);
    Button rescanButton = findViewById(R.id.cancelButton);

    formatLabel.setText(formatName != null ? formatName : "");
    textView.setText(text);

    if (mLaunchedForResult) {
      actionButton.setText("Copy to Zettel");
      actionButton.setOnClickListener(v -> {
        setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_RESULT, mScannedText));
        finish();
      });
    } else {
      actionButton.setText("Copy to Clipboard");
      actionButton.setOnClickListener(v -> {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Scanned barcode", mScannedText);
        clipboard.setPrimaryClip(clip);
        ToastsHelper.showToast(this, "Copied to clipboard");
      });
    }

    rescanButton.setOnClickListener(v -> launchScanner());
  }
}