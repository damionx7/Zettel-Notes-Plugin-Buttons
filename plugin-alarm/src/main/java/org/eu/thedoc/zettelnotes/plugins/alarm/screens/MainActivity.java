package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.eu.thedoc.zettelnotes.broadcasts.AbstractPluginReceiver;
import org.eu.thedoc.zettelnotes.plugins.alarm.BuildConfig;
import org.eu.thedoc.zettelnotes.plugins.alarm.R;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.DatabaseRepository;
import org.eu.thedoc.zettelnotes.plugins.alarm.screens.Adapter.Listener;

public class MainActivity
    extends AppCompatActivity {

  private static final int REQ_CODE_PERMISSION_POST_NOTIFICATION = 1;
  private static final int REQ_CODE_PERMISSION_SCHEDULE_EXACT_ALARM = 2;
  private static final int REQ_CODE_PERMISSION_ZETTEL_BROADCAST_PERMISSION = 3;

  private DatabaseRepository mRepository;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mRepository = new DatabaseRepository(this);

    setTitle("Alarms");

    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    Adapter adapter = new Adapter(new Listener() {
      @Override
      public void onClick(AlarmModel model) {
        AbstractPluginReceiver.IntentBuilder intentBuilder = AbstractPluginReceiver.IntentBuilder.getInstance().setActionOpenUri().setUri(
            model.getFileUri()).setEdit(true).setLineIndexes(model.getIndexes()).setRepository(model.getCategory());
        if (BuildConfig.DEBUG) {
          intentBuilder.setDebug();
        }
        startActivity(intentBuilder.build());
      }

      @Override
      public boolean onLongPress(AlarmModel model) {
        //toggle alarm
        return false;
      }
    });
    recyclerView.setAdapter(adapter);
    LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
    recyclerView.setLayoutManager(manager);
    mRepository.getLiveData().observe(this, data -> adapter.submitData(getLifecycle(), data));
    //check for required android permisisons
    checkPermissions();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    if (permissionNotGranted("org.eu.thedoc.zettelnotes.permission.broadcast")) {
      requestPermission("org.eu.thedoc.zettelnotes.permission.broadcast", REQ_CODE_PERMISSION_ZETTEL_BROADCAST_PERMISSION);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU && permissionNotGranted(permission.POST_NOTIFICATIONS)) {
      requestPermission(permission.POST_NOTIFICATIONS, REQ_CODE_PERMISSION_POST_NOTIFICATION);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.S && permissionNotGranted(permission.SCHEDULE_EXACT_ALARM)) {
      requestPermission(permission.SCHEDULE_EXACT_ALARM, REQ_CODE_PERMISSION_SCHEDULE_EXACT_ALARM);
    }
  }

  public boolean permissionNotGranted(String permission) {
    return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
  }

  public void requestPermission(String permission, int requestCode) {
    ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
  }
}

