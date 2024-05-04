package org.eu.thedoc.zettelnotes.buttons.scanner;

import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL;
import static org.eu.thedoc.zettelnotes.buttons.scanner.MainActivity.FILE_PROVIDER;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.Pdf;
import java.io.File;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class ButtonActivity
    extends AppCompatActivity {

  public static final String ERROR_STRING = "intent-error";

  private ProgressBar mProgressBar;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location);
    mProgressBar = findViewById(R.id.progressBar);
    mProgressBar.setIndeterminate(true);

    GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setResultFormats(RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build();

    GmsDocumentScanner scanner = GmsDocumentScanning.getClient(options);

    ActivityResultLauncher<IntentSenderRequest> scannerLauncher = registerForActivityResult(new StartIntentSenderForResult(), result -> {
      if (result.getResultCode() == RESULT_OK) {
        GmsDocumentScanningResult gmsDocumentScanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
        if (gmsDocumentScanningResult != null) {
          Pdf pdf = gmsDocumentScanningResult.getPdf();
          if (pdf != null) {

            Uri pdfUri = pdf.getUri();
            File file = new File(pdfUri.getPath());

            Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), FILE_PROVIDER, file, file.getName());
            Intent intent = new Intent();
            intent.setDataAndType(fileUri, "application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //now set result and exit activity
            setResult(RESULT_OK, intent);
            finish();
          } else {
            setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "pdf is null"));
            ToastsHelper.showToast(this, "Error: pdf is null");
            finish();
          }
        } else {
          setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "gmsDocumentScanningResult is null"));
          ToastsHelper.showToast(this, "Error: gmsDocumentScanningResult is null");
          finish();
        }
      }
    });
    scanner
        .getStartScanIntent(this)
        .addOnSuccessListener(intentSender -> scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
        .addOnFailureListener(e -> {
          setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, e.toString()));
          ToastsHelper.showToast(ButtonActivity.this, "Error: " + e.toString());
          finish();
        });
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mProgressBar != null) {
      mProgressBar.setVisibility(View.GONE);
    }
  }

}
