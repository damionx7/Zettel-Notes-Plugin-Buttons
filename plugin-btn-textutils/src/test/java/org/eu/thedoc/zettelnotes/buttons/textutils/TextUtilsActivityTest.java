package org.eu.thedoc.zettelnotes.buttons.textutils;

import static org.eu.thedoc.zettelnotes.buttons.textutils.TextUtilsActivity.sortLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class TextUtilsActivityTest {

  @Test
  void testSortLines() {
    // Ascending alphabetical sort
    assertEquals("Apple\nbanana\ncherry", sortLines("banana\nApple\ncherry", true));
    // Descending alphabetical sort
    assertEquals("cherry\nbanana\nApple", sortLines("banana\nApple\ncherry", false));
    // Empty input returns empty string
    assertEquals("", sortLines("", true));
    // Null input returns null
    assertNull(sortLines(null, true));
    // Single line remains unchanged
    assertEquals("onlyline", sortLines("onlyline", true));
    // Duplicate lines sorted with case-insensitive order
    assertEquals("apple\nbanana\ndog\ndog", sortLines("dog\napple\ndog\nbanana", true));
    // Blank lines are ignore
    assertEquals("Apple\nbanana\ncherry", sortLines("banana\n\nApple\n\ncherry", true));
    // Markdown headings sorted (symbol included in comparison)
    assertEquals("# Alpha\n## Beta\n### Gamma", sortLines("### Gamma\n# Alpha\n## Beta", true));
    // Markdown unordered list items sorted
    assertEquals("- Apple\n- Banana\n- Cherry", sortLines("- Banana\n- Cherry\n- Apple", true));
    // Markdown task list items sorted
    assertEquals("- [ ] Learn\n- [x] Done\n- [x] Review", sortLines("- [x] Review\n- [ ] Learn\n- [x] Done", true));
    // Mixed bullet symbols
    assertEquals("* Apple\n- Banana\n+ Cherry", sortLines("- Banana\n+ Cherry\n* Apple", true));
    // Lines with leading spaces (they affect sorting)
    assertEquals("  alpha\n beta\napple", sortLines(" beta\napple\n  alpha", true));
    // Sorting lines with numbers
    assertEquals("1. First\n2. Second\n10. Tenth", sortLines("2. Second\n10. Tenth\n1. First", true));
    // Lines that differ only by case
    assertEquals("Apple\napple\nbanana", sortLines("banana\nApple\napple", true));
    // Lines with symbols and punctuation
    assertEquals("!alert\n?question\n_underscore", sortLines("_underscore\n!alert\n?question", true));
  }

  @Test
  public void taskFormattingTests() {
    // Should retain correctly formatted tasks
    assertEquals("- [ ] Task\n- [ ] Task 2", TextUtilsActivity.formatToTaskList("- [ ] Task\n- [ ] Task 2"));
    // Should normalize task with leading space
    assertEquals("- [ ] Task\n- [ ] Task 2", TextUtilsActivity.formatToTaskList("   - [ ] Task\n- [ ] Task 2"));
    // Should convert unicode ☐ to markdown task list
    assertEquals("- [ ] Task\n- [ ] Task 2", TextUtilsActivity.formatToTaskList("☐  Task\n☐  Task 2"));
    // Should convert spaced unicode ☐ to markdown task list
    assertEquals("- [ ] Task\n- [ ] Task 2", TextUtilsActivity.formatToTaskList("  ☐  Task\n   ☐  Task 2"));
    // Should format plain lines as markdown task list
    assertEquals("- [ ] Task\n- [ ] Task 2", TextUtilsActivity.formatToTaskList("Task\nTask 2"));
    // Should handle only whitespace lines and normalize to single newline
    assertEquals("\n", TextUtilsActivity.formatToTaskList("    \n   "));
    // Should format spaced lines as markdown task list
    assertEquals("- [ ] Task\n- [ ] Task 2", TextUtilsActivity.formatToTaskList("   Task\n   Task 2"));
    // Should remove markdown task list syntax
    assertEquals("Task\nTask 2", TextUtilsActivity.removeTaskList("- [ ] Task\n- [ ] Task 2"));
  }
}