package org.eu.thedoc.zettelnotes.plugins.alarm.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Calendar;
import java.util.Objects;

@Entity(indices = {@Index(value = {"id"}, unique = true)})
public class AlarmModel {

  @PrimaryKey(autoGenerate = true)
  private int id;
  private String text;
  @TypeConverters(DateConverter.class)
  private Calendar calendar;
  private String recurrence;
  private String category;
  private String file;

  @Override
  public int hashCode() {
    return Objects.hash(id, text, calendar, category, file, recurrence);
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
    return id == that.id && Objects.equals(text, that.text) && Objects.equals(calendar, that.calendar) && Objects.equals(category,
        that.category) && Objects.equals(file, that.file) && Objects.equals(recurrence, that.recurrence);
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

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getRecurrence() {
    return recurrence;
  }

  public void setRecurrence(String recurrence) {
    this.recurrence = recurrence;
  }
}
