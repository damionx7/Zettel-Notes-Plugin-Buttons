package org.eu.thedoc.zettelnotes.buttons.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.eu.thedoc.zettelnotes.buttons.chat.CustomTextView.Listener;

public class ChatActivity
    extends AppCompatActivity
    implements Listener {

  public static final String ERROR_STRING = "intent-error";

  public static final String INTENT_EXTRA_BUTTON_MODE = "intent-extra-button-mode";
  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";

  private OpenAI openai;

  private final List<ChatMessage> mChatMessages = new ArrayList<>();
  private MessageAdapter mMessageAdapter;

  private ImageButton sendButton;

  private SharedPreferences sharedPreferences;
  private String prompt;
  private String apiKey;
  private boolean isGpt4;

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
    prompt = sharedPreferences.getString(getString(R.string.prefs_system_prompt_key), "You a helpful writer.");
    apiKey = sharedPreferences.getString(getString(R.string.prefs_api_key), "");
    isGpt4 = !apiKey.isEmpty() && sharedPreferences.getBoolean(getString(R.string.prefs_gpt_4_model_key), false);
  }

  private String getTextSelected() {
    return getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
  }

  private boolean isButtonMode() {
    return getIntent().getBooleanExtra(INTENT_EXTRA_BUTTON_MODE, false);
  }

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sharedPreferences = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
    initializeSharedPrefs();

    EditText editText = findViewById(R.id.edit_text);
    editText.setText(getTextSelected());
    editText.requestFocus();

    sendButton = findViewById(R.id.send_button);
    mMessageAdapter = new MessageAdapter();
    mMessageAdapter.setListener(this);

    ListView listView = findViewById(R.id.list_view);
    listView.setAdapter(mMessageAdapter);

    OkHttpClient.Builder client = new OkHttpClient.Builder();
    client.writeTimeout(30, TimeUnit.SECONDS);
    client.connectTimeout(30, TimeUnit.SECONDS);
    if (apiKey.isEmpty()) {
      apiKey = BuildConfig.OPENAI_API_KEY;
      showToast("Using demo api key. This can stop working anytime. Please set your Open AI Api key in settings.");
      Log.w("ChatActivity", "Using demo api key");
    }

    openai = new OpenAI(apiKey, null, client.build());

    ChatMessage promptMessage = ChatMessage.toSystemMessage(prompt);
    mChatMessages.add(promptMessage);

    sendButton.setOnClickListener(v -> {
      sendButton.setEnabled(false);
      String text = editText.getText().toString();
      if (!text.isEmpty()) {
        editText.setText("");
        addMessage(ChatMessage.toUserMessage(text));

        ChatRequest chatRequest = ChatRequest
            .builder()
            .model(isGpt4 ? ChatModels.GPT_4 : ChatModels.GPT_3_5)
            .messages(mChatMessages)
            .temperature(0.7f)
            .build();
        sendMessage(chatRequest);
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    initializeSharedPrefs();
  }

  private void addMessage(ChatMessage prompt) {
    mChatMessages.add(prompt);
    mMessageAdapter.addItem(prompt);
  }

  //https://github.com/CJCrafter/ChatGPT-Java-API/wiki/Java#Streaming-Chat-Completion
  private void sendMessage(ChatRequest request) {
    StringBuilder stringBuilder = new StringBuilder();
    int pos = mMessageAdapter.addItem(ChatMessage.toAssistantMessage(""));
    openai.streamChatCompletionAsync(request, chatResponse -> {
      String response = chatResponse.get(0).getDelta();
      stringBuilder.append(response);
      //Log.v("DELTA: ", response);
      runOnUiThread(() -> {
        mMessageAdapter.updateItem(pos, ChatMessage.toAssistantMessage(stringBuilder.toString()));
        if (chatResponse.get(0).getFinishReason() != null) {
          mChatMessages.add(chatResponse.get(0).getMessage());
          sendButton.setEnabled(true);
        }
      });
    }, openAIError -> {
      if (openAIError != null) {
        Log.e("ERROR", openAIError.toString());
        showToast(openAIError.toString());
      }
      sendButton.setEnabled(true);
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

  private void showToast(String text) {
    runOnUiThread(() -> {
      if (!text.isEmpty()) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onMenuItemInsertClick(String text) {
    showToast(text);
    setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_INSERT_TEXT, text));
    finish();
  }

  @Override
  public void onMenuItemReplaceClick(String text) {
    showToast(text);
    setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_REPLACE_TEXT, text));
    finish();
  }

  @Override
  public boolean enableInsertMenuItem() {
    return isButtonMode();
  }

  @Override
  public boolean enableReplaceMenuItem() {
    return getTextSelected() != null && !getTextSelected().isEmpty();
  }
}