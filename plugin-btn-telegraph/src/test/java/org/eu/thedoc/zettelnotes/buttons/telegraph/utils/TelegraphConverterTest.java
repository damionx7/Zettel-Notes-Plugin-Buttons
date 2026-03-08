package org.eu.thedoc.zettelnotes.buttons.telegraph.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TelegraphConverterTest {

  @Test
  public void testHeading() {

    String md = "# Hello";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"h1\""));
    assertTrue(json.contains("Hello"));
  }

  @Test
  public void testMultipleHeadings() {

    String md = "# H1\n## H2\n### H3";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"h1\""));
    assertTrue(json.contains("\"tag\": \"h2\""));
    assertTrue(json.contains("\"tag\": \"h3\""));
  }

  @Test
  public void testParagraph() {

    String md = "This is a paragraph.";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"p\""));
    assertTrue(json.contains("paragraph"));
  }

  @Test
  public void testBoldItalic() {

    String md = "**bold** *italic*";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("strong"));
    assertTrue(json.contains("em"));
  }

  @Test
  public void testNestedFormatting() {

    String md = "**bold _italic inside bold_**";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("strong"));
    assertTrue(json.contains("em"));
  }

  @Test
  public void testStrike() {

    String md = "~~deleted~~";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"s\""));
  }

  @Test
  public void testInlineCode() {

    String md = "Use `printf()` function";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"code\""));
    assertTrue(json.contains("printf"));
  }

  @Test
  public void testLink() {

    String md = "[OpenAI](https://openai.com)";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("href"));
    assertTrue(json.contains("https://openai.com"));
  }

  @Test
  public void testLinkInsideParagraph() {

    String md = "Visit [OpenAI](https://openai.com) today.";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"a\""));
    assertTrue(json.contains("OpenAI"));
  }

  @Test
  public void testBulletList() {

    String md = "- A\n- B\n- C";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"ul\""));
    assertTrue(json.contains("\"tag\": \"li\""));
  }

  @Test
  public void testOrderedList() {

    String md = "1. One\n2. Two\n3. Three";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"ol\""));
    assertTrue(json.contains("One"));
    assertTrue(json.contains("Two"));
  }

  @Test
  public void testNestedList() {

    String md = """
                - A
                - B
                - C""";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"ul\""));
    assertTrue(json.contains("A"));
    assertTrue(json.contains("B"));
  }

  @Test
  public void testTaskList() {

    String md = "- [x] done\n- [ ] todo";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("checked"));
    assertTrue(json.contains("unchecked"));
  }

  @Test
  public void testBlockQuote() {

    String md = "> This is a quote";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"blockquote\""));
    assertTrue(json.contains("quote"));
  }

  @Test
  public void testNestedBlockQuoteFormatting() {

    String md = "> **Bold quote**";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("blockquote"));
    assertTrue(json.contains("strong"));
  }

  @Test
  public void testCodeBlock() {

    String md = "```java\nSystem.out.println(\"hi\");\n```";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("pre"));
    assertTrue(json.contains("code"));
    assertTrue(json.contains("println"));
  }

  @Test
  public void testHorizontalRule() {

    String md = "---";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"hr\""));
  }

  @Test
  public void testTable() {

    String md = """
                | A | B |
                |---|---|
                | 1 | 2 |""";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("\"tag\": \"table\""));
    assertTrue(json.contains("\"tag\": \"tr\""));
    assertTrue(json.contains("\"tag\": \"td\""));
  }

  @Test
  public void testComplexMarkdown() {

    String md = """
                # Title
                
                Paragraph with **bold**, *italic*, and [link](https://example.com).
                
                - Item 1
                - Item 2
                
                > Quote here
                
                ```java
                System.out.println("Hello");
                ```""";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("h1"));
    assertTrue(json.contains("strong"));
    assertTrue(json.contains("em"));
    assertTrue(json.contains("a"));
    assertTrue(json.contains("ul"));
    assertTrue(json.contains("blockquote"));
    assertTrue(json.contains("code"));
  }

  @Test
  public void testEmptyMarkdown() {

    String md = "";

    String json = TelegraphConverter.convert(md);

    assertNotNull(json);
  }

  @Test
  public void testPlainText() {

    String md = "Just plain text";

    String json = TelegraphConverter.convert(md);

    assertTrue(json.contains("plain text"));
  }

}