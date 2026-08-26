package org.eu.thedoc.zettelnotes.buttons.llm;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class RemoteLlmClient {

  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
      .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
      .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
      .build();

  public static String chat(String url, String apiKey, String prompt, int maxTokens, float temperature) throws IOException {

    JSONObject message = new JSONObject();
    try {
      message.put("role", "user");
      message.put("content", prompt);

      JSONArray messages = new JSONArray();
      messages.put(message);

      JSONObject body = new JSONObject();
      body.put("model", "default");
      body.put("messages", messages);
      body.put("max_tokens", maxTokens);
      body.put("temperature", temperature);

      Request.Builder requestBuilder = new Request.Builder()
          .url(url)
          .post(RequestBody.create(body.toString(), JSON));

      if (apiKey != null && !apiKey.isEmpty()) {
        requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
      }

      try (Response response = CLIENT
          .newCall(requestBuilder.build())
          .execute()) {
        if (!response.isSuccessful() || response.body() == null) {
          throw new IOException("HTTP " + response.code());
        }
        JSONObject json = new JSONObject(response
            .body()
            .string());
        return json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content");
      }

    } catch (org.json.JSONException e) {
      throw new IOException("Bad response format: " + e.getMessage());
    }
  }
}