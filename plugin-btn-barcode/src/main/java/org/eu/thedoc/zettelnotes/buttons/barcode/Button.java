package org.eu.thedoc.zettelnotes.buttons.barcode;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.ButtonInterface;

public class Button
    extends ButtonInterface {

  private final ActivityResultListener mScanResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      String scanned = result
          .getData()
          .getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
      Log.v(getClass().getName(), "Got barcode content");
      if (mCallback != null && scanned != null && !scanned.isEmpty()) {
        mCallback.insertText(scanned);
      }
    } else if (result.getData() != null) {
      String error = result
          .getData()
          .getStringExtra(ScanActivity.ERROR_STRING);
      Log.e(getClass().getName(), error);
    }
  };

  private final ActivityResultListener mGenerateResultListener = result -> {
    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
      Object uriExtra = result
          .getData()
          .getParcelableExtra(GenerateActivity.INTENT_EXTRA_URI);
      Log.v(getClass().getName(), "Got generated barcode image");
      if (mCallback != null && uriExtra != null) {
        mCallback.insertUri((android.net.Uri) uriExtra);
      }
    } else if (result.getData() != null) {
      String error = result
          .getData()
          .getStringExtra(GenerateActivity.ERROR_STRING);
      Log.e(getClass().getName(), error);
    }
  };

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.setActivityResultListener(mScanResultListener);
        mCallback.startActivityForResult(new Intent("org.eu.thedoc.zettelnotes.intent.buttons.barcode.scan"));
      }
    }


    @Override
    public boolean onLongClick() {
      if (mCallback != null) {
        String text = mCallback.getTextSelected(true);
        if (text == null || text.isEmpty()) {
          return false;
        }
        mCallback.setActivityResultListener(mGenerateResultListener);
        Intent intent = new Intent("org.eu.thedoc.zettelnotes.intent.buttons.barcode.generate");
        intent.putExtra(GenerateActivity.INTENT_EXTRA_TEXT, text);
        mCallback.startActivityForResult(intent);
        return true;
      }
      return false;
    }

  };

  @Override
  public String getName() {
    return "Barcode";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
