package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.eu.thedoc.zettelnotes.broadcasts.AbstractPluginReceiver;
import org.eu.thedoc.zettelnotes.broadcasts.AbstractPluginReceiver.IntentBuilder;
import org.eu.thedoc.zettelnotes.plugins.alarm.BuildConfig;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.DatabaseRepository;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.AlarmAdapter.Listener;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.AlarmHelper;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class MainActivity
    extends AppCompatActivity {

  private static final int REQ_CODE_PERMISSION_POST_NOTIFICATION = 1;
  private static final int REQ_CODE_PERMISSION_SCHEDULE_EXACT_ALARM = 2;
  private static final int REQ_CODE_PERMISSION_ZETTEL_BROADCAST_PERMISSION = 3;

  private DatabaseRepository mRepository;
  private AlarmHelper mAlarmHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mRepository = new DatabaseRepository(this);
    mAlarmHelper = new AlarmHelper(this);

    setTitle("Alarms");

    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    AlarmAdapter alarmAdapter = new AlarmAdapter(new Listener() {
      @Override
      public void onClick(AlarmModel model) {
        IntentBuilder intentBuilder = IntentBuilder
            .getInstance()
            .setActionOpenUri()
            .setUri(model.getFileUri())
            .setEdit(true)
            .setLineIndexes(model.getIndexes())
            .setRepository(model.getCategory());
        startActivity(intentBuilder.build());
      }

      @Override
      public boolean onLongPress(AlarmModel model) {
        //toggle alarm
        return false;
      }

      @Override
      public void onDelete(AlarmModel model) {
        //unschedule
        mAlarmHelper.unschedule(List.of(model));
        //remove from database
        mRepository.delete(model);
        //comment alarm from note
        AbstractPluginReceiver.IntentBuilder intentBuilder = AbstractPluginReceiver.IntentBuilder
            .getInstance()
            .setActionOpenUri()
            .setUri(model.getFileUri())
            .setLineIndexes(model.getIndexes())
            .setActionOpenAndReplace(model.transformCommented())
            .setEdit(true)
            .setRepository(model.getCategory());
        startActivity(intentBuilder.build());
      }

      @Override
      public void onCheckTask(AlarmModel model) {
        //unschedule
        mAlarmHelper.unschedule(List.of(model));
        //remove from database
        mRepository.delete(model);
        //tick and comment alarm from note
        AbstractPluginReceiver.IntentBuilder intentBuilder = AbstractPluginReceiver.IntentBuilder
            .getInstance()
            .setActionOpenUri()
            .setUri(model.getFileUri())
            .setLineIndexes(model.getIndexes())
            .setActionOpenAndReplace(model.transformChecked())
            .setEdit(true)
            .setRepository(model.getCategory());
        startActivity(intentBuilder.build());
      }
    });
    recyclerView.setAdapter(alarmAdapter);
    LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
    recyclerView.setLayoutManager(manager);
    //set data
    mRepository.getLiveData().observe(this, data -> alarmAdapter.submitData(getLifecycle(), data));
    //check for required android permissions
    checkPermissions();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (permissions.length == 0) {
      return;
    }
    String permission = permissions[0];
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      //permission granted
    } else {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
        //permission declined
      } else {
        //permission declined dont ask again
      }
    }
  }

  private void checkPermissions() {
    Log.v(getClass().getName(), "checkPermissions");

    if (BuildConfig.DEBUG) {
      if (permissionNotGranted("org.eu.thedoc.zettelnotes.debug.permission.broadcast")) {
        requestPermission("org.eu.thedoc.zettelnotes.debug.permission.broadcast", REQ_CODE_PERMISSION_ZETTEL_BROADCAST_PERMISSION);
      }
    } else {
      if (permissionNotGranted("org.eu.thedoc.zettelnotes.permission.broadcast")) {
        requestPermission("org.eu.thedoc.zettelnotes.permission.broadcast", REQ_CODE_PERMISSION_ZETTEL_BROADCAST_PERMISSION);
      }
    }

    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU && permissionNotGranted(permission.POST_NOTIFICATIONS)) {
      requestPermission(permission.POST_NOTIFICATIONS, REQ_CODE_PERMISSION_POST_NOTIFICATION);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.S && !mAlarmHelper.canScheduleAlarms()) {
      Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
      startActivity(intent);
      ToastsHelper.showToast(this, "Allow alarms permission for notification to be on exact time.");
    }
  }

  public boolean permissionNotGranted(String permission) {
    return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
  }

  public void requestPermission(String permission, int requestCode) {
    ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
  }
}

