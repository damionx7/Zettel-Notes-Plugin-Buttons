package org.eu.thedoc.zettelnotes.plugins.base.utils;

import android.content.Context;
import android.util.TypedValue;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Utils {

  public static final Charset CHARSET = Charset.forName("UTF-8");

  public static int getPx(Context context, int dp) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
  }

  public static String readFromInputStream(
      @Nullable InputStream inputStream) throws Exception {
    return new String(readFromInputStreamAndReturnByteArray(inputStream), CHARSET);
  }

  public static byte[] readFromInputStreamAndReturnByteArray(
      @Nullable InputStream inputStream) throws Exception {
    try (inputStream) {
      final ByteArrayOutputStream result = new ByteArrayOutputStream();
      final byte[] buffer = new byte[1024];
      for (int length; (length = inputStream.read(buffer)) != -1; ) {
        result.write(buffer, 0, length);
      }
      return result.toByteArray();
    }
  }


}
