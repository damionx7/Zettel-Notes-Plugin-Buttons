package org.eu.thedoc.zettelnotes.buttons.scanner;

import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.Pdf;
import java.io.File;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class MainActivity
    extends AppCompatActivity {

  public static final String FILE_PROVIDER = "org.eu.thedoc.zettelnotes.buttons.scanner.provider.file";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    AppCompatButton button = findViewById(R.id.button_scan_document);

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
            int pageCount = pdf.getPageCount();

            File file = new File(pdfUri.getPath());

            Log.v(getClass().getName(), "Got Uri " + pdfUri.toString());

            //now share the file
            Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), FILE_PROVIDER, file, file.getName());
            Intent chooser = createShareFileIntent(fileUri);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chooser);
          }
        }
      }
    });

    button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        scanner
            .getStartScanIntent(MainActivity.this)
            .addOnSuccessListener(intentSender -> scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(
                  @NonNull Exception e) {
                Log.e(getClass().getName(), e.toString());
                ToastsHelper.showToast(MainActivity.this, e.toString());
              }
            });
      }
    });
  }

  private Intent createShareFileIntent(Uri fileUri) {
    Intent mResultIntent = new Intent();
    mResultIntent.setDataAndType(fileUri, "application/pdf");
    mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
    mResultIntent.setAction(Intent.ACTION_SEND);
    mResultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    return Intent.createChooser(mResultIntent, "Share File");
  }

}


