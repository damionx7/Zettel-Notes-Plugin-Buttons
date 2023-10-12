package org.eu.thedoc.zettelnotes.plugins.alarm.utils;

import android.util.Log;

public class Logger {

  public static void verbose(Class<?> clazz, String message) {
    Log.v(clazz.getSimpleName(), message);
  }
}
