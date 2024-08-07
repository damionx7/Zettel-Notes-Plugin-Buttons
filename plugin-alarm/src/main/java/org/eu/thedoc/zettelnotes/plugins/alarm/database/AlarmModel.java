package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import android.util.Log;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

@TypeConverters(TypeConverter.class)
@Entity(indices = {@Index(value = {"id"}, unique = true)})
public class AlarmModel {

  public static final String TYPE_NOTE = "note";
  public static final String TYPE_TASK = "task";

  @PrimaryKey(autoGenerate = true)
  private int id;
  private String text;
  private Calendar calendar;
  private String recurrence;
  private String category;
  private String fileTitle;
  private String fileUri;
  private int[] indexes;
  private boolean checked;
  private String type;

  public String getFileTitle() {
    return fileTitle;
  }

  public void setFileTitle(String fileTitle) {
    this.fileTitle = fileTitle;
  }

  public String getFileUri() {
    return fileUri;
  }

  public void setFileUri(String fileUri) {
    this.fileUri = fileUri;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, text, calendar, category, fileTitle, fileUri, recurrence, Arrays.hashCode(indexes), checked, type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlarmModel that = (AlarmModel) o;
    return Objects.equals(text, that.text) && Objects.equals(calendar, that.calendar) && Objects.equals(category,
        that.category) && Objects.equals(fileTitle, that.fileTitle) && Objects.equals(fileUri, that.fileUri) && Objects.equals(recurrence,
        that.recurrence) && Arrays.equals(indexes, that.indexes) && Objects.equals(checked, that.checked) && Objects.equals(type,
        that.type);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public void setCalendar(Calendar calendar) {
    this.calendar = calendar;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getRecurrence() {
    return recurrence;
  }

  public void setRecurrence(String recurrence) {
    this.recurrence = recurrence;
  }

  public int[] getIndexes() {
    return indexes;
  }

  public void setIndexes(int[] indexes) {
    this.indexes = indexes;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String transformCommented() {
    try {
      if (text.contains("\n")) {
        String[] strings = text.split("\n");
        return String.format("%%%%%s%%%%\n%s", strings[0], (strings.length == 1 || strings[1].isEmpty()) ? "" : strings[1]);
      } else {
        return String.format("%%%%%s%%%%", text);
      }
    } catch (Exception e) {
      Log.e(getClass().getName(), e + ", text: " + text);
      return text;
    }
  }

  public String transformChecked() {
    return transformCommented().replace("- [ ] ", "- [x] ");
  }

  public enum Recurrence {

    MIN("m"),
    HOUR("h"),
    DAY("d"),
    WEEK("w"),
    MONTH("M"),
    YEAR("y");

    final String constant;

    Recurrence(String constant) {
      this.constant = constant;
    }

    public String getConstant() {
      return this.constant;
    }
  }
}