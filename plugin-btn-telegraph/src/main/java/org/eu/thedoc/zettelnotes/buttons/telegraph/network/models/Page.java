package org.eu.thedoc.zettelnotes.buttons.telegraph.network.models;

import com.google.gson.annotations.SerializedName;

public class Page {

  @SerializedName("path")
  public String path;

  @SerializedName("url")
  public String url;

  @SerializedName("title")
  public String title;

  @SerializedName("views")
  public int views;
}