package org.eu.thedoc.zettelnotes.plugins.base.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {

  private static final Object LOCK = new Object();
  private static AppExecutor sInstance;

  private final Executor mainThread;
  private final ExecutorService diskIO;
  private final ExecutorService networkIO;

  public AppExecutor(ExecutorService diskIO, ExecutorService networkIO, Executor mainThread) {
    this.diskIO = diskIO;
    this.networkIO = networkIO;
    this.mainThread = mainThread;
  }

  public static AppExecutor getInstance() {
    if (sInstance == null) {
      synchronized (LOCK) {
        sInstance = new AppExecutor(Executors.newFixedThreadPool(10), Executors.newFixedThreadPool(3), new MainThreadExecutor());
      }
    }
    return sInstance;
  }

  public ExecutorService diskIO() {
    return diskIO;
  }

  public Executor mainThread() {
    return mainThread;
  }

  public ExecutorService networkIO() {
    return networkIO;
  }

  private static class MainThreadExecutor
      implements Executor {

    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(
        @NonNull Runnable runnable) {
      mainThreadHandler.post(runnable);
    }
  }
}