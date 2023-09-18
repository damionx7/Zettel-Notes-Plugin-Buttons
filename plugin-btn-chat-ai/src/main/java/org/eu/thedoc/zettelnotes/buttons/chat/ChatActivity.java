package org.eu.thedoc.zettelnotes.buttons.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.ChatUser;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class ChatActivity
    extends AppCompatActivity {

  public static final String ERROR_STRING = "intent-error";

  public static final String INTENT_EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String INTENT_EXTRA_REPLACE_TEXT = "intent-extra-text-replace";
  public static final String INTENT_EXTRA_INSERT_TEXT = "intent-extra-insert-text";
  private final List<String> mChatList = new ArrayList<>();
  private ProgressBar mProgressBar;
  private List<ChatMessage> messages;
  private OpenAI openai;
  private ArrayAdapter<String> mAdapter;
  private ImageButton sendButton;
  private ListView listView;

  @Override
  protected void onCreate(
      @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    listView = findViewById(R.id.list_view);
    listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

    EditText editText = findViewById(R.id.edit_text);
    sendButton = findViewById(R.id.send_button);

    mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mChatList);
    listView.setAdapter(mAdapter);

    //mProgressBar = findViewById(R.id.progressBar);
    //mProgressBar.setIndeterminate(true);
    String txtSelected = getIntent().getStringExtra(INTENT_EXTRA_TEXT_SELECTED);
    editText.setText(txtSelected);

    OkHttpClient.Builder client = new OkHttpClient.Builder();
    client.writeTimeout(30, TimeUnit.SECONDS);
    client.connectTimeout(30, TimeUnit.SECONDS);
    openai = new OpenAI("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", null, client.build());

    ChatMessage prompt = ChatMessage.toSystemMessage("Assume the role of a professional writer.");
    messages = new ArrayList<>(List.of(prompt));

    sendButton.setOnClickListener(v -> {
      sendButton.setEnabled(false);
      String text = editText.getText().toString();
      if (!text.isEmpty()) {
        editText.setText("");
        mChatList.add(text);
        mAdapter.notifyDataSetChanged();
        messages.add(new ChatMessage(ChatUser.USER, text));
        ChatRequest chatRequest = ChatRequest.builder().model("gpt-3.5-turbo").messages(messages).temperature(0.7f).build();
        sendMessage(chatRequest);
      }
    });
    //finish();
  }

  //https://github.com/CJCrafter/ChatGPT-Java-API/wiki/Java#Streaming-Chat-Completion
  private void sendMessage(ChatRequest request) {
    StringBuilder stringBuilder = new StringBuilder();
    mChatList.add("");
    int index = mChatList.size() - 1;
    openai.streamChatCompletionAsync(request, chatResponse -> {
      String response = chatResponse.get(0).getDelta();
      stringBuilder.append(response);
      Log.v("DELTA: ", response);
      runOnUiThread(() -> {
        mChatList.set(index, stringBuilder.toString());
        mAdapter.notifyDataSetChanged();
        if (chatResponse.get(0).getFinishReason() != null) {
          messages.add(chatResponse.get(0).getMessage());
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
    if (mProgressBar != null) {
      mProgressBar.setVisibility(View.GONE);
    }
    setResult(RESULT_OK);
    super.onStop();
  }

  private void showToast(String text) {
    runOnUiThread(() -> {
      if (!text.isEmpty()) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
      }
    });
  }
}
