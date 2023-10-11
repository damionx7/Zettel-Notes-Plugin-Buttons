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

  @Query("DELETE FROM alarmmodel WHERE category = :category AND fileUri = :fileUri")
  int delete(String category, String fileUri);

  @Delete
  int deleteAllModels(List<AlarmModel> models);

  @Transaction
  @Query("SELECT * FROM alarmmodel ORDER BY calendar DESC")
  PagingSource<Integer, AlarmModel> getAllViaPaging();

  @Transaction
  @Query("SELECT * FROM alarmmodel WHERE id IN (:indexes) AND calendar >= :after")
  List<AlarmModel> getAll(List<Long> indexes, long after);
}
