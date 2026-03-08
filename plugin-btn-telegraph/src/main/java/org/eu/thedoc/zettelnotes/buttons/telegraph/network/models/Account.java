package org.eu.thedoc.zettelnotes.buttons.telegraph.network.models;

import com.google.gson.annotations.SerializedName;

public class Account {

  @SerializedName("short_name")
  public String shortName;

  @SerializedName("author_name")
  public String authorName;

  @SerializedName("access_token")
  public String accessToken;
}