package org.eu.thedoc.zettelnotes.buttons.imgbb;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Api {

  @Multipart
  @POST("1/upload")
  Call<ApiResponse> upload(
      @Query("key") String key,
      @Part MultipartBody.Part image,
      @Part("name") RequestBody name);
}
