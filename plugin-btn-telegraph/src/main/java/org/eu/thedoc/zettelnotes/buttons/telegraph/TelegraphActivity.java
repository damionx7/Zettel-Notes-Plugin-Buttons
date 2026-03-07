package org.eu.thedoc.zettelnotes.buttons.telegraph;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class TelegraphActivity extends AppCompatActivity {

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  public static final String ERROR_STRING = "intent-error";

  public static final String PREFS = "_settings_telegraph";
  public static final String PREFS_ACCESS_TOKEN = "access_token";

  private ProgressBar mProgressBar;
  private EditText etTitle, etContent;
  private Button btnPublish, btnInsertUrl;
  private TextView tvUrl;

  private SharedPreferences mPrefs;
  private TelegraphApi api;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    mPrefs = getSharedPreferences(PREFS, MODE_PRIVATE);

    mProgressBar = findViewById(R.id.progressBar);
    etTitle = findViewById(R.id.etTitle);
    etContent = findViewById(R.id.etContent);
    btnPublish = findViewById(R.id.btnPublish);
    btnInsertUrl = findViewById(R.id.btnInsertUrl);
    tvUrl = findViewById(R.id.tvUrl);

    String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    if (txtSelected != null && !txtSelected.isEmpty()) {
      etContent.setText(txtSelected);
    }

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.telegra.ph/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    api = retrofit.create(TelegraphApi.class);

    btnPublish.setOnClickListener(v -> publishArticle());

    btnInsertUrl.setOnClickListener(v -> {
      String url = tvUrl.getText().toString();
      setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, url));
      finish();
    });
  }

  private void publishArticle() {
    String title = etTitle.getText().toString().trim();
    String content = etContent.getText().toString().trim();

    if (title.isEmpty() || content.isEmpty()) {
      ToastsHelper.showToast(this, "Title and content cannot be empty");
      return;
    }

    mProgressBar.setVisibility(View.VISIBLE);
    btnPublish.setEnabled(false);

    String accessToken = mPrefs.getString(PREFS_ACCESS_TOKEN, null);
    if (accessToken == null) {
      createAccountAndPublish(title, content);
    } else {
      createPage(accessToken, title, content);
    }
  }

  private void createAccountAndPublish(String title, String content) {
    api.createAccount("ZettelNotes", "Zettel Notes").enqueue(new Callback<TelegraphResponse<Account>>() {
      @Override
      public void onResponse(Call<TelegraphResponse<Account>> call, Response<TelegraphResponse<Account>> response) {
        if (response.isSuccessful() && response.body() != null && response.body().ok) {
          String accessToken = response.body().result.access_token;
          mPrefs.edit().putString(PREFS_ACCESS_TOKEN, accessToken).apply();
          createPage(accessToken, title, content);
        } else {
          handleError("Failed to create Telegraph account");
        }
      }

      @Override
      public void onFailure(Call<TelegraphResponse<Account>> call, Throwable t) {
        handleError(t.getMessage());
      }
    });
  }

  private void createPage(String accessToken, String title, String content) {
    // Format content as simple JSON array of nodes. For simple text we can just pass the string,
    // but Telegra.ph expects an array of Node objects. A simple string is also a valid Node.
    String contentJson = new com.google.gson.Gson().toJson(java.util.Collections.singletonList(content));

    api.createPage(accessToken, title, contentJson, true).enqueue(new Callback<TelegraphResponse<Page>>() {
      @Override
      public void onResponse(Call<TelegraphResponse<Page>> call, Response<TelegraphResponse<Page>> response) {
        mProgressBar.setVisibility(View.GONE);
        btnPublish.setEnabled(true);

        if (response.isSuccessful() && response.body() != null && response.body().ok) {
          String url = response.body().result.url;
          tvUrl.setText(url);
          tvUrl.setVisibility(View.VISIBLE);
          btnInsertUrl.setVisibility(View.VISIBLE);
        } else {
          handleError("Failed to create Telegraph page");
        }
      }

      @Override
      public void onFailure(Call<TelegraphResponse<Page>> call, Throwable t) {
        mProgressBar.setVisibility(View.GONE);
        btnPublish.setEnabled(true);
        handleError(t.getMessage());
      }
    });
  }

  private void handleError(String message) {
    mProgressBar.setVisibility(View.GONE);
    btnPublish.setEnabled(true);
    ToastsHelper.showToast(this, message != null ? message : "An error occurred");
  }

  public interface TelegraphApi {
    @FormUrlEncoded
    @POST("createAccount")
    Call<TelegraphResponse<Account>> createAccount(
        @Field("short_name") String shortName,
        @Field("author_name") String authorName
    );

    @FormUrlEncoded
    @POST("createPage")
    Call<TelegraphResponse<Page>> createPage(
        @Field("access_token") String accessToken,
        @Field("title") String title,
        @Field("content") String content,
        @Field("return_content") boolean returnContent
    );
  }

  public static class TelegraphResponse<T> {
    public boolean ok;
    public T result;
    public String error;
  }

  public static class Account {
    public String short_name;
    public String author_name;
    public String author_url;
    public String access_token;
    public String auth_url;
  }

  public static class Page {
    public String path;
    public String url;
    public String title;
    public String description;
    public String author_name;
    public String author_url;
    public String image_url;
    public String content;
    public int views;
    public boolean can_edit;
  }
}
