package org.eu.thedoc.zettelnotes.buttons.textutils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TextUtilsActivityTest {

  @Test
  public void formatTaskWithTaskTest() {
    String lines = "- [ ] Task\n- [ ] Task 2";
    assertEquals(lines, TextUtilsActivity.formatToTaskList(lines));
  }

  @Test
  public void formatTaskWithTaskAndSpaceTest() {
    String lines = "   - [ ] Task\n- [ ] Task 2";
    String result = "- [ ] Task\n- [ ] Task 2";
    assertEquals(result, TextUtilsActivity.formatToTaskList(lines));
  }

  @Test
  public void formatTaskWithUnicodeTest() {
    String lines = "☐  Task\n☐  Task 2";
    String result = "- [ ] Task\n- [ ] Task 2";
    assertEquals(result, TextUtilsActivity.formatToTaskList(lines));
  }

  @Test
  public void formatTaskWithUnicodeAndSpaceTest() {
    String lines = "  ☐  Task\n   ☐  Task 2";
    String result = "- [ ] Task\n- [ ] Task 2";
    assertEquals(result, TextUtilsActivity.formatToTaskList(lines));
  }

  @Test
  public void formatTaskTest() {
    String lines = "Task\nTask 2";
    String result = "- [ ] Task\n- [ ] Task 2";
    assertEquals(result, TextUtilsActivity.formatToTaskList(lines));
  }

  //todo: fix clearing empty lines
  @Test
  public void formatTaskNoStringTest() {
    String lines = "    \n   ";
    String result = "\n";
    assertEquals(result, TextUtilsActivity.formatToTaskList(lines));
  }

  @Test
  public void formatTaskWithSpaceTest() {
    String lines = "   Task\n   Task 2";
    String result = "- [ ] Task\n- [ ] Task 2";
    assertEquals(result, TextUtilsActivity.formatToTaskList(lines));
  }

  @Test
  public void removeTaskTest() {
    String lines = "- [ ] Task\n- [ ] Task 2";
    String result = "Task\nTask 2";
    assertEquals(result, TextUtilsActivity.removeTaskList(lines));
  }

}