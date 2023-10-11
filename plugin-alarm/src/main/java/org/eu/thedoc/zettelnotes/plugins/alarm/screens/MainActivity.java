package org.eu.thedoc.zettelnotes.plugins.alarm.screens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
  }
}

