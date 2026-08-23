package org.eu.thedoc.zettelnotes.buttons.imgbb;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;

public class SettingsActivity
    extends BaseActivity {

  public static final String PREFS = "_prefs";
  public static final String PREF_API_KEY = "api_key";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    int p = dp(20);
    root.setPadding(p, p, p, p);

    TextView title = new TextView(this);
    title.setText("ImgBB API key");
    title.setTextSize(20);
    root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    TextView help = new TextView(this);
    help.setText("""
                 Enter your ImgBB API key. It is stored only in this app's private preferences.\n\n" + "Create/get the key at api.imgbb.com.
                 """);
    root.addView(help, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    EditText key = new EditText(this);
    key.setHint("Paste API key");
    key.setSingleLine(true);
    key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    key.setText(getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_API_KEY, ""));
    root.addView(key, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    Button save = new Button(this);
    save.setText("Save");
    save.setOnClickListener(v -> {
      String value = key
          .getText()
          .toString()
          .trim();
      getSharedPreferences(PREFS, MODE_PRIVATE)
          .edit()
          .putString(PREF_API_KEY, value)
          .apply();
      Toast
          .makeText(this, value.isEmpty() ? "API key cleared" : "API key saved", Toast.LENGTH_SHORT)
          .show();
      finish();
    });
    root.addView(save, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    setContentView(root);
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }
}
