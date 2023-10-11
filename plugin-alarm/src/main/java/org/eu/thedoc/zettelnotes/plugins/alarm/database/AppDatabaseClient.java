package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import android.content.Context;
import androidx.room.Room;

public class AppDatabaseClient {

  public final static String DB_NAME = "_plugin_alarm";
  private static volatile AppDatabaseClient sInstance;
  private final AppDatabase mAppDatabase;

  private AppDatabaseClient(Context context) {
    mAppDatabase = Room.databaseBuilder(context, AppDatabase.class, DB_NAME).fallbackToDestructiveMigration().addMigrations(
        AppDatabase.MIGRATIONS).build();
  }

  public static AppDatabaseClient getInstance(Context context) {
    AppDatabaseClient client = sInstance;
    if (client == null) {
      synchronized (AppDatabaseClient.class) {
        client = sInstance;
        if (client == null) {
          sInstance = client = new AppDatabaseClient(context);
        }
      }
    }
    return client;
  }

  public AppDatabase getAppDatabase() {
    return mAppDatabase;
  }

}
