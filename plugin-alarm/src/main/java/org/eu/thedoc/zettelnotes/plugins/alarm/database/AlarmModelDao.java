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
  List<Long> insert(List<AlarmModel> models);

  @Delete
  int delete(AlarmModel model);

  @Delete
  void delete(List<AlarmModel> models);

  @Query("DELETE FROM alarmmodel WHERE category = :category AND fileUri = :fileUri")
  void delete(String category, String fileUri);

  @Query("DELETE FROM alarmmodel WHERE category = :category AND fileUri IN (:uris)")
  void delete(String category, List<String> uris);

  @Transaction
  @Query("SELECT * FROM alarmmodel ORDER BY calendar DESC")
  PagingSource<Integer, AlarmModel> get();

  @Transaction
  @Query("SELECT * FROM alarmmodel WHERE id IN (:indexes) AND calendar >= :after")
  List<AlarmModel> get(List<Long> indexes, long after);

  @Query("SELECT * FROM alarmmodel WHERE category = :category AND fileUri = :uri")
  List<AlarmModel> get(String category, String uri);

  @Query("SELECT * FROM alarmmodel WHERE category = :category AND fileUri IN (:uris)")
  List<AlarmModel> get(String category, List<String> uris);
}
