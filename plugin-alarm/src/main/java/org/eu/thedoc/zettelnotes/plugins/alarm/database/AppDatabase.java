package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;

@Database(entities = {AlarmModel.class}, version = 5)
public abstract class AppDatabase
    extends RoomDatabase {

  public static final Migration[] MIGRATIONS = new Migration[]{};

  public abstract AlarmModelDao mAlarmModelDao();
}
