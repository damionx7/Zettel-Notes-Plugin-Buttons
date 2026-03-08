package org.eu.thedoc.zettelnotes.buttons.telegraph.network.models;

import com.google.gson.annotations.SerializedName;

public class TelegraphResponse<T> {

  @SerializedName("ok")
  public boolean ok;

  @SerializedName("result")
  public T result;

  @SerializedName("error")
  public String error;

  public boolean isSuccess() {
    return ok && result != null;
  }
}