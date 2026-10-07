package org.eu.thedoc.zettelnotes.buttons.mermaid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;
import org.eu.thedoc.zettelnotes.plugins.base.utils.Utils;
import org.json.JSONObject;

/**
 * Editor screen: Mermaid code on top, live rendered preview below. Rendering happens offline with the bundled mermaid.js in a WebView that
 * cannot reach the network. Toolbar: Templates menu and Render. Bottom bar: insert as image file, image link, or code block.
 */
public class ButtonActivity
    extends BaseActivity {

  public static final String ERROR_STRING = "intent-error-string";
  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-replace-text";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";

  public static final String FILE_PROVIDER = "org.eu.thedoc.zettelnotes.buttons.mermaid.provider.file";

  private static final String TEMPLATES_ASSET = "mermaid_templates.json";
  private static final String RENDER_PAGE = "file:///android_asset/mermaid_render.html";
  private static final long RENDER_DEBOUNCE_MS = 800;
  private static final long RENDER_TIMEOUT_MS = 30_000;
  private static final long CACHE_MAX_AGE_MS = 24L * 60 * 60 * 1000;
  private static final int PREVIEW_MAX_SIDE = 2048;

  private static final int MENU_RENDER = 1;
  private static final int MENU_TEMPLATE_BASE = 100;

  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private final Runnable mRenderRunnable = this::renderNow;
  private final Runnable mTimeout = this::onRenderTimeout;

  private final List<String> mTemplateNames = new ArrayList<>();
  private final List<String> mTemplateCodes = new ArrayList<>();

  private EditText mEditor;
  private TextView mStatus;
  private TextView mPlaceholder;
  private ImageView mPreview;
  private ProgressBar mProgress;
  private View mInsertImage;
  private View mInsertLink;
  private View mInsertCode;
  private WebView mWebView;

  private boolean mHadSelection;
  private boolean mPageLoaded;
  private boolean mPendingImage;
  private boolean mDone;
  private boolean mDestroyed;

  private int mSeq;
  private String mRenderingCode;
  private byte[] mPng;
  private String mPngCode;

  public static String wrapInFence(String body) {
    String trimmed = body.endsWith("\n") ? body.substring(0, body.length() - 1) : body;
    return "```mermaid\n" + trimmed + "\n```\n";
  }

  private static Bitmap decodePreview(byte[] png) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeByteArray(png, 0, png.length, options);
    int longest = Math.max(options.outWidth, options.outHeight);
    int sample = 1;
    while (longest / sample > PREVIEW_MAX_SIDE) {
      sample *= 2;
    }
    options.inJustDecodeBounds = false;
    options.inSampleSize = sample;
    return BitmapFactory.decodeByteArray(png, 0, png.length, options);
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);

    mEditor = findViewById(R.id.editor_text);
    mStatus = findViewById(R.id.editor_status);
    mPlaceholder = findViewById(R.id.preview_placeholder);
    mPreview = findViewById(R.id.preview_image);
    mProgress = findViewById(R.id.preview_progress);
    mInsertImage = findViewById(R.id.btn_insert_image);
    mInsertLink = findViewById(R.id.btn_insert_link);
    mInsertCode = findViewById(R.id.btn_insert_code);
    mWebView = findViewById(R.id.render_web_view);

    loadTemplates();
    setupToolbar();

    // Prefill with the selected diagram code; on rotation the editor restores its own text
    String selected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    mHadSelection = selected != null && !selected.isEmpty();
    if (savedInstanceState == null && mHadSelection) {
      String code = MermaidInk.stripFence(selected);
      mEditor.setText(code);
      mEditor.setSelection(code.length());
    }

    mEditor.addTextChangedListener(new TextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        updateButtons();
        scheduleRender();
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}
    });

    mInsertImage.setOnClickListener(v -> onInsertImage());
    mInsertLink.setOnClickListener(v -> onInsertLink());
    mInsertCode.setOnClickListener(v -> onInsertCode());
    updateButtons();

    setupWebView();
  }

  private void loadTemplates() {
    try {
      String json = Utils.readFromInputStream(getAssets().open(TEMPLATES_ASSET));
      LinkedHashMap<String, String> templates = new Gson().fromJson(json, new TypeToken<LinkedHashMap<String, String>>() {}.getType());
      mTemplateNames.addAll(templates.keySet());
      mTemplateCodes.addAll(templates.values());
    } catch (Exception e) {
      ToastsHelper.showToast(getApplicationContext(), "Error loading templates: " + e);
    }
  }

  private void setupToolbar() {
    MaterialToolbar toolbar = findViewById(R.id.editor_toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());

    Menu menu = toolbar.getMenu();
    SubMenu templates = menu.addSubMenu(Menu.NONE, Menu.NONE, 0, "Templates");
    templates
        .getItem()
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    for (int i = 0; i < mTemplateNames.size(); i++) {
      templates.add(Menu.NONE, MENU_TEMPLATE_BASE + i, i, mTemplateNames.get(i));
    }
    MenuItem render = menu.add(Menu.NONE, MENU_RENDER, 1, "Render");
    render.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

    toolbar.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == MENU_RENDER) {
        renderNow();
        return true;
      }
      int index = id - MENU_TEMPLATE_BASE;
      if (index >= 0 && index < mTemplateCodes.size()) {
        insertTemplate(mTemplateCodes.get(index));
        return true;
      }
      return false;
    });
  }

  /**
   * Picking a template clears the editor first, so the template replaces whatever was there.
   */
  private void insertTemplate(String template) {
    mEditor.setText(template);
    mEditor.setSelection(template.length());
    renderNow();
  }

  private String currentCode() {
    return mEditor
        .getText()
        .toString()
        .trim();
  }

  // ---- Rendering

  private void updateButtons() {
    boolean hasCode = !currentCode().isEmpty();
    mInsertImage.setEnabled(hasCode);
    mInsertLink.setEnabled(hasCode);
    mInsertCode.setEnabled(hasCode);
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void setupWebView() {
    WebSettings settings = mWebView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setBlockNetworkLoads(true);

    mWebView.addJavascriptInterface(new RenderBridge(), "Android");
    mWebView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        mPageLoaded = true;
        renderNow();
      }
    });
    mWebView.loadUrl(RENDER_PAGE);
  }

  private void scheduleRender() {
    mHandler.removeCallbacks(mRenderRunnable);
    if (currentCode().isEmpty()) {
      renderNow();
    } else {
      mHandler.postDelayed(mRenderRunnable, RENDER_DEBOUNCE_MS);
    }
  }

  private void renderNow() {
    mHandler.removeCallbacks(mRenderRunnable);
    if (mDestroyed || mDone) {
      return;
    }
    String code = currentCode();
    if (code.isEmpty()) {
      clearPreview();
      return;
    }
    if (!mPageLoaded) {
      return; // onPageFinished renders the current text
    }
    mSeq++;
    mRenderingCode = code;
    mProgress.setVisibility(View.VISIBLE);
    mHandler.removeCallbacks(mTimeout);
    mHandler.postDelayed(mTimeout, RENDER_TIMEOUT_MS);
    mWebView.evaluateJavascript("renderMermaid(" + JSONObject.quote(code) + "," + mSeq + ")", null);
  }

  private void clearPreview() {
    mSeq++; // drop any render still in flight
    mHandler.removeCallbacks(mTimeout);
    mPendingImage = false;
    mPng = null;
    mPngCode = null;
    mPreview.setImageDrawable(null);
    mPlaceholder.setVisibility(View.VISIBLE);
    mProgress.setVisibility(View.GONE);
    hideStatus();
  }

  private void onRenderSucceeded(int seq, byte[] png,
      @Nullable Bitmap preview) {
    if (seq != mSeq || mDestroyed || mDone) {
      return;
    }
    if (preview == null) {
      onRenderFailed(seq, "Could not decode rendered image");
      return;
    }
    mHandler.removeCallbacks(mTimeout);
    mProgress.setVisibility(View.GONE);
    mPng = png;
    mPngCode = mRenderingCode;
    mPreview.setImageBitmap(preview);
    mPlaceholder.setVisibility(View.GONE);
    hideStatus();

    if (mPendingImage) {
      if (mPngCode.equals(currentCode())) {
        mPendingImage = false;
        returnImage();
      } else {
        renderNow(); // edited again while rendering
      }
    }
  }

  private void onRenderFailed(int seq, String message) {
    if (seq != mSeq || mDestroyed || mDone) {
      return;
    }
    mHandler.removeCallbacks(mTimeout);
    mProgress.setVisibility(View.GONE);
    mPendingImage = false;
    showStatus(message); // the last good preview stays visible
  }

  private void onRenderTimeout() {
    mProgress.setVisibility(View.GONE);
    mPendingImage = false;
    showStatus("Rendering timed out");
  }

  private void showStatus(String message) {
    mStatus.setText(message);
    mStatus.setVisibility(View.VISIBLE);
  }

  private void hideStatus() {
    mStatus.setVisibility(View.GONE);
  }

  // ---- Insert actions

  private void onInsertImage() {
    String code = currentCode();
    if (code.isEmpty()) {
      return;
    }
    if (mPng != null && code.equals(mPngCode)) {
      returnImage();
    } else {
      mPendingImage = true; // inserts as soon as the up-to-date render arrives
      renderNow();
    }
  }

  private void onInsertLink() {
    returnText(MermaidInk.toImageMarkdown(currentCode()));
  }

  private void onInsertCode() {
    returnText(wrapInFence(currentCode()));
  }

  /**
   * With a selection the result replaces it; otherwise it is inserted at the cursor.
   */
  private void returnText(String text) {
    if (mDone) {
      return;
    }
    mDone = true;
    Intent result = new Intent();
    result.putExtra(mHadSelection ? INTENT_EXTRA_REPLACE_TEXT : INTENT_EXTRA_INSERT_TEXT, text);
    setResult(RESULT_OK, result);
    finish();
  }

  /**
   * The app imports the PNG itself from the content Uri (same hand-off as the scanner plugin).
   */
  private void returnImage() {
    if (mDone) {
      return;
    }
    try {
      File dir = new File(getCacheDir(), "mermaid");
      if (!dir.exists() && !dir.mkdirs()) {
        throw new IllegalStateException("Cannot create cache dir");
      }
      File[] old = dir.listFiles();
      if (old != null) {
        for (File f : old) {
          if (System.currentTimeMillis() - f.lastModified() > CACHE_MAX_AGE_MS) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
          }
        }
      }
      String name = "mermaid_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";
      File file = new File(dir, name);
      try (FileOutputStream out = new FileOutputStream(file)) {
        out.write(mPng);
      }
      Uri uri = FileProvider.getUriForFile(getApplicationContext(), FILE_PROVIDER, file, file.getName());

      mDone = true;
      Intent intent = new Intent();
      intent.setDataAndType(uri, "image/png");
      intent.putExtra(Intent.EXTRA_STREAM, uri);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      setResult(RESULT_OK, intent);
      finish();
    } catch (Exception e) {
      showStatus("Error: " + e);
    }
  }

  @Override
  protected void onDestroy() {
    mDestroyed = true;
    mHandler.removeCallbacksAndMessages(null);
    if (mWebView != null) {
      ViewGroup parent = (ViewGroup) mWebView.getParent();
      if (parent != null) {
        parent.removeView(mWebView);
      }
      mWebView.destroy();
      mWebView = null;
    }
    super.onDestroy();
  }

  /**
   * Called from the render page; methods run on a WebView background thread, so results are posted to the UI thread.
   */
  private class RenderBridge {

    @JavascriptInterface
    public void onResult(int seq, String base64Png) {
      try {
        byte[] png = Base64.decode(base64Png, Base64.DEFAULT);
        Bitmap preview = decodePreview(png);
        runOnUiThread(() -> onRenderSucceeded(seq, png, preview));
      } catch (Exception e) {
        runOnUiThread(() -> onRenderFailed(seq, "Error: " + e));
      }
    }

    @JavascriptInterface
    public void onError(int seq, String message) {
      runOnUiThread(() -> onRenderFailed(seq, message));
    }
  }

}