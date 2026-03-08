package org.eu.thedoc.zettelnotes.buttons.telegraph.network;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetryCall {

  private static final int DEFAULT_RETRIES = 3;

  public static <T> void enqueue(Call<T> call, Callback<T> callback) {
    enqueue(call, callback, DEFAULT_RETRIES);
  }

  public static <T> void enqueue(Call<T> call, Callback<T> callback, int retries) {

    call.enqueue(new Callback<T>() {

      @Override
      public void onResponse(
          @NonNull Call<T> call,
          @NonNull Response<T> response) {
        callback.onResponse(call, response);
      }

      @Override
      public void onFailure(
          @NonNull Call<T> call,
          @NonNull Throwable t) {

        if (retries > 0) {

          Call<T> cloned = call.clone();

          enqueue(cloned, callback, retries - 1);

        } else {

          callback.onFailure(call, t);
        }
      }
    });
  }
}