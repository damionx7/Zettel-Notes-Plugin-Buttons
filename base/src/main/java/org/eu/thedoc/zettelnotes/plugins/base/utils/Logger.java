package org.eu.thedoc.zettelnotes.plugins.base.utils;

import android.util.Log;
import org.eu.thedoc.zettelnotes.interfaces.BuildConfig;

public class Logger {

  public static void verbose(Class<?> clazz, String message) {
    if (BuildConfig.DEBUG) {
      Log.v(clazz.getSimpleName(), message);
    }
  }

  public static void warn(Class<?> clazz, String message) {
    if (BuildConfig.DEBUG) {
      Log.w(clazz.getSimpleName(), message);
    }
  }

  public static void err(Class<?> clazz, String message) {
    Log.e(clazz.getSimpleName(), message);
  }
}
