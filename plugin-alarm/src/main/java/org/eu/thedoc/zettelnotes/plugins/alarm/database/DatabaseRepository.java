package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import android.content.Context;
import android.util.Log;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.AlarmHelper;

public class DatabaseRepository {

  private static final ConcurrentHashMap<String, Lock> mHashMap = new ConcurrentHashMap<>();
  private static final String LOCK_KEY_SCAN = "key_scan";
  private static final String LOCK_KEY_DELETE = "key_delete";

  private final AlarmModelDao mDao;
  private final ExecutorService mDiskIO;
  private final PagingConfig mPagingConfig;
  private final AlarmHelper mAlarmHelper;

  public DatabaseRepository(Context context) {
    mDao = AppDatabaseClient.getInstance(context).getAppDatabase().mAlarmModelDao();
    mDiskIO = Executors.newFixedThreadPool(3);
    mPagingConfig = new PagingConfig(20, 40, true, 20, 200, 200);
    mAlarmHelper = new AlarmHelper(context);
  }

  public LiveData<PagingData<AlarmModel>> getLiveData() {
    return PagingLiveData.getLiveData(new Pager<>(mPagingConfig, mDao::get));
  }

  public void addAll(String category, String fileUri, List<AlarmModel> models) {
    if (models.size() == 0) {
      return;
    }
    mDiskIO.execute(() -> {
      Lock currentLock = mHashMap.computeIfAbsent(LOCK_KEY_SCAN, s -> new ReentrantLock());
      if (currentLock.tryLock()) {
        try {
          unScheduleAndDelete(category, fileUri);
          scheduleAndInsert(models);
        } finally {
          currentLock.unlock();
          mHashMap.remove(LOCK_KEY_SCAN);
        }
      } else {
        Log.v(getClass().getName(), "addAll already running");
      }
    });
  }

  public void deleteAll(String category, ArrayList<String> uris) {
    if (uris.isEmpty()) {
      return;
    }
    mDiskIO.execute(() -> {
      Lock currentLock = mHashMap.computeIfAbsent(LOCK_KEY_DELETE, s -> new ReentrantLock());
      if (currentLock.tryLock()) {
        try {
          unScheduleAndDelete(category, uris);
        } finally {
          currentLock.unlock();
          mHashMap.remove(LOCK_KEY_DELETE);
        }
      } else {
        Log.e(getClass().getName(), "deleteAll already running");
      }
    });
  }

  @WorkerThread
  private void scheduleAndInsert(List<AlarmModel> models) {
    //insert new models
    List<Long> indexes = mDao.insert(models);
    Log.v(getClass().getName(), String.format("inserted %s/%s alarms in database", indexes.size(), models.size()));
    //schedule alarms
    List<AlarmModel> alarmModels = mDao.get(indexes, Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis());
    mAlarmHelper.schedule(alarmModels);
  }

  @WorkerThread
  private void unScheduleAndDelete(String category, ArrayList<String> uris) {
    //unschedule existing alarms
    List<AlarmModel> models = unscheduleAlarms(category, uris);
    Log.v(getClass().getName(), String.format("Got %s database alarms for category: %s uris: %s", models.size(), category, uris.size()));
    //delete from database
    mDao.delete(models);
  }

  @WorkerThread
  private void unScheduleAndDelete(String category, String fileUri) {
    //unSchedule existing alarms
    List<AlarmModel> previousModels = unscheduleAlarms(category, fileUri);
    Log.v(getClass().getName(), String.format("Got %s database alarms for category:%s uri: %s", previousModels.size(), category, fileUri));
    //delete previous models
    mDao.delete(previousModels);
  }

  @WorkerThread
  private List<AlarmModel> unscheduleAlarms(String category, List<String> uris) {
    List<AlarmModel> models = mDao.get(category, uris);
    mAlarmHelper.unschedule(models);
    return models;
  }

  @WorkerThread
  private List<AlarmModel> unscheduleAlarms(String category, String fileUri) {
    List<AlarmModel> models = mDao.get(category, fileUri);
    mAlarmHelper.unschedule(models);
    return models;
  }

  public void delete(AlarmModel model) {
    mDiskIO.execute(() -> mDao.delete(model));
  }
}
