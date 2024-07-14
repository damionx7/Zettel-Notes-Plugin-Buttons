package org.eu.thedoc.zettelnotes.buttons.latex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.LinkedHashMap;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;
import org.eu.thedoc.zettelnotes.plugins.base.utils.Utils;

public class ButtonActivity
    extends AppCompatActivity {

  public static final String ERROR_STRING = "intent-error-string";
  public static final String DATA_STRING = "data-string";

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    showLatexDialog();
  }

  private void showLatexDialog() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("Latex Symbols");
    builder.setOnCancelListener(dialog -> {
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Canceled Dialog"));
      finish();
    });
    View view = getLayoutInflater().inflate(R.layout.dialog_recycler_view, null);
    builder.setView(view);

    AppCompatEditText searchView = view.findViewById(R.id.dialog_edit_text);
    searchView.setVisibility(View.GONE);

    RecyclerView recyclerView = view.findViewById(R.id.dialog_recycler_view);

    Adapter adapter = new Adapter((symbol, desc) -> {
      setResult(RESULT_OK, new Intent().putExtra(DATA_STRING, desc));
      finish();
    });
    recyclerView.setAdapter(adapter);
    GridLayoutManager layoutManager = new GridLayoutManager(this, 6);
    recyclerView.setLayoutManager(layoutManager);
    view.post(() -> {
      int width = view.getWidth();
      if (width > 0) {
        layoutManager.setSpanCount(width / Utils.getPx(this, 56));
      }
    });
    //feed data
    try {
      String json = Utils.readFromInputStream(getAssets().open("latex_symbols.json"));
      final LinkedHashMap<String, String> treeMap = new Gson().fromJson(json, new TypeToken<LinkedHashMap<String, String>>() {}.getType());
      adapter.submitData(treeMap);
      builder.show();
    } catch (Exception e) {
      ToastsHelper.showToast(getApplicationContext(), "Error: " + e);
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Error: " + e));
      finish();
    }
  }

}