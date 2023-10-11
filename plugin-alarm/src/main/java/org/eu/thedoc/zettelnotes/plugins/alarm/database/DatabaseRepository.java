package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.AlarmUtils;

public class DatabaseRepository {

  private static final ConcurrentHashMap<String, Lock> mHashMap = new ConcurrentHashMap<>();
  private static final String LOCK_KEY = "key";

  private final Context mContext;
  private final AlarmModelDao mDao;
  private final ExecutorService mDiskIO;
  private final PagingConfig mPagingConfig;
  private final AlarmUtils mAlarmUtils;

  public DatabaseRepository(Context context) {
    mContext = context;
    mDao = AppDatabaseClient.getInstance(context).getAppDatabase().mAlarmModelDao();
    mDiskIO = Executors.newFixedThreadPool(3);
    mPagingConfig = new PagingConfig(20, 40, true, 20, 200, 200);
    mAlarmUtils = new AlarmUtils(context);
  }

  public LiveData<PagingData<AlarmModel>> getLiveData() {
    return PagingLiveData.getLiveData(new Pager<>(mPagingConfig, mDao::getAllViaPaging));
  }

  public void addAll(List<AlarmModel> models) {
    if (models.size() == 0) {
      return;
    }
    mDiskIO.execute(() -> {
      Lock currentLock = mHashMap.computeIfAbsent(LOCK_KEY, s -> new ReentrantLock());
      if (currentLock.tryLock()) {
        try {
          //remove previous note alarms
          mDao.delete(models.get(0).getCategory(), models.get(0).getFileUri());
          //insert new alarms
          List<Long> indexes = mDao.insertAll(models);
          Log.v("Alarm::Repository", "inserted " + indexes.size() + "/" + models.size());
          //schedule alarms for models with id (future or current events only)
          List<AlarmModel> alarmModels = mDao.getAll(indexes, Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis());
          mAlarmUtils.schedule(alarmModels);
        } finally {
          currentLock.unlock();
          mHashMap.remove(LOCK_KEY);
        }
      } else {
        Log.e("ALARM::Repository", "addAll already running");
      }
    });
  }
}
