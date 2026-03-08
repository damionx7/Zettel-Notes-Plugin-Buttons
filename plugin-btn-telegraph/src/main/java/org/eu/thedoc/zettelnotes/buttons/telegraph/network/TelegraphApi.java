package org.eu.thedoc.zettelnotes.buttons.telegraph.network;

import org.eu.thedoc.zettelnotes.buttons.telegraph.network.models.Account;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.models.Page;
import org.eu.thedoc.zettelnotes.buttons.telegraph.network.models.TelegraphResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TelegraphApi {

  @FormUrlEncoded
  @POST("createAccount")
  Call<TelegraphResponse<Account>> createAccount(
      @Field("short_name") String shortName,
      @Field("author_name") String authorName);

  @FormUrlEncoded
  @POST("createPage")
  Call<TelegraphResponse<Page>> createPage(
      @Field("access_token") String accessToken,
      @Field("title") String title,
      @Field("content") String content,
      @Field("return_content") boolean returnContent);
}
