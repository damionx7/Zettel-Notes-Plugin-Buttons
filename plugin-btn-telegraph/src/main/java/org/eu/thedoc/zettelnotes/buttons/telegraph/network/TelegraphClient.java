package org.eu.thedoc.zettelnotes.buttons.telegraph.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TelegraphClient {

  private static Retrofit retrofit;

  public static Retrofit get() {

    if (retrofit == null) {

      OkHttpClient client = new OkHttpClient.Builder()
          .connectTimeout(20, TimeUnit.SECONDS)
          .readTimeout(20, TimeUnit.SECONDS)
          .retryOnConnectionFailure(true)
          .build();

      retrofit = new Retrofit.Builder()
          .baseUrl("https://api.telegra.ph/")
          .client(client)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
    }

    return retrofit;
  }
}