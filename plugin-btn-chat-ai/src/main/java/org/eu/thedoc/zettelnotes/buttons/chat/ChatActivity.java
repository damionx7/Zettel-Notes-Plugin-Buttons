package org.eu.thedoc.zettelnotes.buttons.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.ChatResponseChunk;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.eu.thedoc.zettelnotes.buttons.chat.CustomTextView.Listener;
import org.eu.thedoc.zettelnotes.plugins.base.utils.AppExecutor;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class ChatActivity
    extends AppCompatActivity
    implements Listener {

  public static final String ERROR_STRING = "intent-error";
  public static final String INTENT_EXTRA_BUTTON_MODE = "intent-extra-button-mode";
  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  private final List<ChatMessage> mChatMessages = new ArrayList<>();
  private final Executor mHandler = AppExecutor.getInstance().mainThread();
  private final Executor mNetworkIO = AppExecutor.getInstance().networkIO();
  private OpenAI openai;
  private MessageAdapter mMessageAdapter;

  private AppCompatImageButton sendButton, mPromptButton;

  private SharedPreferences mSharedPreferences;
  private String mSystemPrompt, mApiKey, mApiUrl, mApiModel;
  private boolean mSingleMessage;
  private float mTemp;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(
      @NonNull MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_settings) {
      Intent intent = new Intent();
      intent.setClass(getApplicationContext(), SettingsActivity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void initializeSharedPrefs() {
    mSystemPrompt = mSharedPreferences.getString(getString(R.string.prefs_system_prompt_key),
        getString(R.string.prefs_default_system_prompt));
    //restore user prompt
    String userPrompt = mSharedPreferences.getString(getString(R.string.prefs_user_selected_system_prompt_key), "");
    if (!userPrompt.isEmpty()) {
      mSystemPrompt = userPrompt;
    }

    mApiKey = mSharedPreferences.getString(getString(R.string.prefs_api_key), "");
    mApiUrl = mSharedPreferences.getString(getString(R.string.prefs_api_url_key), "");
    mApiModel = mSharedPreferences.getString(getString(R.string.prefs_api_model_key), getString(R.string.model_gpt_4));
    if (mApiModel.equals(getString(R.string.model_custom))) {
      //set custom model
      mApiModel = mSharedPreferences.getString(getString(R.string.prefs_custom_model_key), getString(R.string.model_gpt_4));
    }

    mSingleMessage = mSharedPreferences.getBoolean(getString(R.string.prefs_single_message_key), false);
    mTemp = mSharedPreferences.getInt(getString(R.string.prefs_temperature_key), 8) / 10f;
  }

  private String getTextSelected() {
    String string = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    if (string != null && !string.isEmpty()) {
      return String.format("\"%s\"", string);
    }
    return "";
  }

  private boolean isButtonMode() {
    return getIntent().getBooleanExtra(INTENT_EXTRA_BUTTON_MODE, false);
  }

  private List<String> getPrompts() {
    String json = mSharedPreferences.getString(getString(R.string.prefs_system_prompts_key), "");
    List<String> prompts = new Gson().fromJson(json, new TypeToken<LinkedList<String>>() {}.getType());
    if (prompts == null) {
      prompts = new LinkedList<>();
    }
    return prompts;
  }

  private void savePrompts(List<String> prompts) {
    mSharedPreferences.edit().putString(getString(R.string.prefs_system_prompts_key), new Gson().toJson(prompts)).apply();
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    EditText editText = findViewById(R.id.edit_text);
    editText.setText(getTextSelected());
    editText.requestFocus();

    mPromptButton = findViewById(R.id.activity_main_button_system_prompt);
    mPromptButton.setOnClickListener(v -> showPromptSelectDialog());
    TooltipCompat.setTooltipText(mPromptButton, mPromptButton.getContentDescription());

    sendButton = findViewById(R.id.send_button);
    mMessageAdapter = new MessageAdapter();
    mMessageAdapter.setListener(this);

    ListView listView = findViewById(R.id.list_view);
    listView.setAdapter(mMessageAdapter);

    mSharedPreferences = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);

    initializeSharedPrefs();
    initializeAiAgent();
    setSystemPrompt(mSystemPrompt);

    sendButton.setOnClickListener(v -> {
      String text = editText.getText().toString();
      if (!text.isEmpty()) {
        ChatMessage message = ChatMessage.toUserMessage(text);
        editText.setText("");
        addMessage(message);

        if (mSingleMessage) {
          //clear prev messages
          setSystemPrompt(mSystemPrompt);
          mChatMessages.add(message);
        }

        ChatRequest chatRequest = ChatRequest.builder().model(mApiModel).messages(mChatMessages).temperature(mTemp).build();
        sendMessage(chatRequest);
        sendButton.setEnabled(false);
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    //
    initializeSharedPrefs();
    initializeAiAgent();
  }

  private void initializeAiAgent() {
    OkHttpClient.Builder client = new OkHttpClient.Builder();
    client.connectTimeout(30, TimeUnit.SECONDS);
    client.readTimeout(30, TimeUnit.SECONDS);
    client.writeTimeout(30, TimeUnit.SECONDS);
    if (BuildConfig.DEBUG) {
      client.addInterceptor(new HttpLoggingInterceptor().setLevel(Level.BODY)).build();
    }
    if (mApiKey.isEmpty()) {
      mApiKey = BuildConfig.OPENAI_API_KEY;
      ToastsHelper.showToast(this, "Using demo api key. This can stop working anytime. Please set your Open AI Api key in settings.");
      Log.w("ChatActivity", "Using demo api key");
    }

    OpenAI.Builder builder = OpenAI.builder();
    if (!mApiUrl.isBlank()) {
      builder.baseUrl(mApiUrl);
    }
    builder.apiKey(mApiKey).client(client.build());
    openai = builder.build();
  }

  private void showPromptSelectDialog() {
    final List<String> prompts = getPrompts();

    AtomicInteger index = new AtomicInteger(prompts.indexOf(mSystemPrompt));
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("Select Prompt");
    ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, prompts);
    builder.setSingleChoiceItems(adapter, index.get(), (dialog, which) -> {
      index.set(which);
      setSystemPrompt(prompts.get(which));
      //
      dialog.dismiss();
    });
    builder.setNeutralButton("Default",
        (dialog, which) -> setSystemPrompt(mSharedPreferences.getString(getString(R.string.prefs_system_prompt_key),
            getString(R.string.prefs_default_system_prompt))));
    builder.setPositiveButton("Add", (dialog, which) -> {
      //
      showPromptEnterDialog();
    });
    builder.setNegativeButton("Remove", (dialog, which) -> {
      prompts.remove(index.get());
      savePrompts(prompts);
      //
      showPromptSelectDialog();
    });
    builder.show();
  }

  private void setSystemPrompt(String prompt) {
    mChatMessages.clear();
    //
    mSystemPrompt = prompt;
    mChatMessages.add(ChatMessage.toSystemMessage(prompt));
    //
    mSharedPreferences.edit().putString(getString(R.string.prefs_user_selected_system_prompt_key), prompt).apply();
  }

  private void showPromptEnterDialog() {
    final EditText editText = new EditText(this);

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("Enter Prompt");
    builder.setView(editText);
    builder.setPositiveButton("Add", (dialog, which) -> {
      List<String> prompts = getPrompts();
      if (editText.length() > 0) {
        prompts.add(editText.getText().toString());
        savePrompts(prompts);
      }
      //
      showPromptSelectDialog();
    });
    builder.show();
  }

  private void addMessage(ChatMessage prompt) {
    mChatMessages.add(prompt);
    mMessageAdapter.addItem(prompt);
  }

  //https://github.com/CJCrafter/ChatGPT-Java-API/wiki/Java#Streaming-Chat-Completion
  private void sendMessage(ChatRequest request) {
    StringBuilder stringBuilder = new StringBuilder();
    int pos = mMessageAdapter.addItem(ChatMessage.toAssistantMessage(""));
    mNetworkIO.execute(() -> {
      try {
        for (ChatResponseChunk chunk : openai.streamChatCompletion(request)) {
          // This is nullable! ChatGPT will return null AT LEAST ONCE PER MESSAGE.
          String response = chunk.get(0).getDeltaContent();
          if (response != null) {
            stringBuilder.append(response);
            runOnUiThread(() -> mMessageAdapter.updateItem(pos, ChatMessage.toAssistantMessage(stringBuilder.toString())));
          }

          // When the response is finished, we can add it to the messages list.
          if (chunk.get(0).isFinished()) {
            mChatMessages.add(chunk.get(0).getMessage());
          }
        }
      } catch (Exception e) {
        Log.e("ERROR", e.toString(), e);
        ToastsHelper.showToast(this, e.getMessage() == null ? e.toString() : e.getMessage());
        runOnUiThread(() -> mMessageAdapter.updateItem(pos, ChatMessage.toAssistantMessage(e.toString())));
      } finally {
        runOnUiThread(() -> sendButton.setEnabled(true));
      }
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    setResult(RESULT_OK);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  @Override
  public void onMenuItemInsertClick(String text) {
    ToastsHelper.showToast(this, text);
    setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, text));
    finish();
  }

  @Override
  public void onMenuItemReplaceClick(String text) {
    ToastsHelper.showToast(this, text);
    setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, text));
    finish();
  }

  @Override
  public boolean enableInsertMenuItem() {
    return isButtonMode();
  }

  @Override
  public boolean enableReplaceMenuItem() {
    return !getTextSelected().isEmpty();
  }
}