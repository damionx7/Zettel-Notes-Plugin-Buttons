package org.eu.thedoc.zettelnotes.buttons.todotxt;

import android.util.Log;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

public class Todo {

  public boolean isChecked;
  public String priority;
  public String completionDate;
  public String dueDate;
  public String task;

  private Todo() {
    //
  }

  public static Todo parse(String text) {
    Todo todo = new Todo();
    Matcher matcher = Regex.TodoTxt.pattern.matcher(text);
    if (matcher.find()) {
      todo.isChecked = text.startsWith("x ");
      todo.priority = matcher.group(3);
      String group5 = matcher.group(5);
      String group7 = matcher.group(7);
      if (group7 == null) {
        todo.dueDate = group5;
      } else {
        todo.completionDate = group5;
        todo.dueDate = group7;
      }
      todo.task = matcher.group(8);
    }
    return todo;
  }

  public static String toString(Todo todo) {
    String checked = todo.isChecked ? "x " : "";
    String priorityText = todo.priority == null ? "" : String.format("(%s) ", todo.priority);
    String completionDateText = todo.completionDate == null ? "" : String.format("%s ", todo.completionDate);
    String dueDateText = todo.dueDate == null ? "" : String.format("%s ", todo.dueDate);
    String taskText = todo.task;
    String string = String.format("%s%s%s%s%s", checked, priorityText, completionDateText, dueDateText, taskText);
    Log.v("TODO", "string " + string);
    return string;
  }
}
