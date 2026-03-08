package org.eu.thedoc.zettelnotes.buttons.telegraph;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.RetryCall;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.TelegraphApi;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.TelegraphClient;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.models.Account;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.models.Page;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.models.TelegraphResponse;
import org.eu.thedoc.zettelnotes.buttons.telegraph.utils.TelegraphConverter;
import org.eu.thedoc.zettelnotes.plugins.base.BaseActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelegraphActivity
    extends BaseActivity {

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";

  public static final String ERROR_STRING = "intent-error";

  private static final String PREFS = "_settings_telegraph";
  private static final String PREFS_ACCESS_TOKEN = "access_token";

  private ProgressBar progressBar;
  private EditText etTitle, etContent;
  private Button btnPublish, btnInsertUrl;
  private TextView tvUrl;

  private SharedPreferences prefs;
  private TelegraphApi api;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
    api = TelegraphClient
        .get()
        .create(TelegraphApi.class);

    progressBar = findViewById(R.id.progressBar);
    etTitle = findViewById(R.id.etTitle);
    etContent = findViewById(R.id.etContent);
    btnPublish = findViewById(R.id.btnPublish);
    btnInsertUrl = findViewById(R.id.btnInsertUrl);
    tvUrl = findViewById(R.id.tvUrl);

    String selected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    if (selected != null) {
      etContent.setText(selected);
    }

    btnPublish.setOnClickListener(v -> publishArticle());

    btnInsertUrl.setOnClickListener(v -> {
      setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, tvUrl
          .getText()
          .toString()));
      finish();
    });
  }

  private void publishArticle() {

    String title = etTitle
        .getText()
        .toString()
        .trim();
    String content = etContent
        .getText()
        .toString()
        .trim();

    if (title.isEmpty() || content.isEmpty()) {
      toast("Title and content required");
      return;
    }

    setLoading(true);

    String token = prefs.getString(PREFS_ACCESS_TOKEN, null);

    if (token == null) {
      createAccount(title, content);
    } else {
      createPage(token, title, content);
    }
  }

  private void createAccount(String title, String content) {
    Call<TelegraphResponse<Account>> call = api.createAccount("zettelnotes", "Zettel Notes");
    RetryCall.enqueue(call, new Callback<>() {
      @Override
      public void onResponse(Call<TelegraphResponse<Account>> call, Response<TelegraphResponse<Account>> response) {
        TelegraphResponse<Account> body = response.body();

        if (body != null && body.isSuccess()) {

          String token = body.result.accessToken;
          prefs
              .edit()
              .putString(PREFS_ACCESS_TOKEN, token)
              .apply();

          createPage(token, title, content);

        } else {

          showError(body != null ? body.error : "Unknown error");
        }
      }

      @Override
      public void onFailure(Call<TelegraphResponse<Account>> call, Throwable t) {
        showError(t.getMessage());
      }
    });
  }

  private void createPage(String token, String title, String text) {

    String contentJson = TelegraphConverter.convert(text);

    Call<TelegraphResponse<Page>> call = api.createPage(token, title, contentJson, true);

    RetryCall.enqueue(call, new Callback<>() {

      @Override
      public void onResponse(
          @NonNull Call<TelegraphResponse<Page>> call,
          @NonNull Response<TelegraphResponse<Page>> response) {

        setLoading(false);

        TelegraphResponse<Page> body = response.body();

        if (body != null && body.isSuccess()) {

          String url = body.result.url;

          tvUrl.setText(url);
          tvUrl.setVisibility(View.VISIBLE);
          btnInsertUrl.setVisibility(View.VISIBLE);

        } else {
          showError(body != null ? body.error : "Unknown error");
        }
      }

      @Override
      public void onFailure(
          @NonNull Call<TelegraphResponse<Page>> call,
          @NonNull Throwable t) {
        setLoading(false);
        showError(t.getMessage());
      }
    });
  }

  private void setLoading(boolean loading) {
    progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    btnPublish.setEnabled(!loading);
  }

  private void showError(TelegraphResponse<?> response) {
    showError(response != null ? response.error : "Unknown error");
  }

  private void showError(String msg) {
    setLoading(false);
    toast(msg);
  }

  private void toast(String msg) {
    Toast
        .makeText(this, msg, Toast.LENGTH_SHORT)
        .show();
  }
}
