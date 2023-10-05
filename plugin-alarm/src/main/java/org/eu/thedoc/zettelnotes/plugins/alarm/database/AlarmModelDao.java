package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

@Dao
public interface AlarmModelDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(AlarmModel model);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  List<Long> insertAll(List<AlarmModel> models);

  @Delete
  int delete(AlarmModel model);

  @Query("DELETE FROM alarmmodel WHERE category = :category AND file = :file")
  int delete(String category, String file);

  @Delete
  int deleteAllModels(List<AlarmModel> models);

  @Query("SELECT * from alarmmodel WHERE category = :category AND file = :file ORDER BY id LIMIT 1")
  AlarmModel getByName(String category, String file);

  @Transaction
  @Query("SELECT * FROM alarmmodel ORDER BY id")
  PagingSource<Integer, AlarmModel> getAllViaPaging();
}
