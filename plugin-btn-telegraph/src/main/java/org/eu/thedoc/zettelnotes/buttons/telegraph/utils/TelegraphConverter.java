package org.eu.thedoc.zettelnotes.buttons.telegraph.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TelegraphConverter {

  private static final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .create();

  private static final Parser parser = Parser
      .builder()
      .extensions(Arrays.asList(TaskListExtension.create(), StrikethroughExtension.create(), TablesExtension.create()))
      .build();

  public static String convert(String markdown) {

    Node document = parser.parse(markdown);

    List<Object> nodes = new ArrayList<>();

    for (Node child = document.getFirstChild(); child != null; child = child.getNext()) {

      Object n = convertNode(child);
      if (n != null) {
        nodes.add(n);
      }

    }

    return gson.toJson(nodes);
  }

  private static Object convertNode(Node node) {

    if (node instanceof Paragraph) {
      return tag("p", children(node));
    }

    if (node instanceof Heading) {
      int level = ((Heading) node).getLevel();
      return tag("h" + level, children(node));
    }

    if (node instanceof BulletList) {
      return tag("ul", listChildren(node));
    }

    if (node instanceof OrderedList) {
      return tag("ol", listChildren(node));
    }

    if (node instanceof TaskListItem item) {

      Map<String, Object> li = tag("li", children(node));

      Map<String, String> attrs = new HashMap<>();
      attrs.put("class", item.isItemDoneMarker() ? "checked" : "unchecked");

      li.put("attrs", attrs);

      return li;
    }

    if (node instanceof ListItem) {
      return tag("li", children(node));
    }

    if (node instanceof FencedCodeBlock code) {

      return tag("pre", Collections.singletonList(tag("code", Collections.singletonList(code
          .getContentChars()
          .toString()))));
    }

    if (node instanceof BlockQuote) {
      return tag("blockquote", children(node));
    }

    if (node instanceof TableBlock) {
      return convertTable((TableBlock) node);
    }

    if (node instanceof ThematicBreak) {
      return tag("hr", null);
    }

    return null;
  }

  private static Object convertInline(Node node) {

    if (node instanceof Text) {
      return ((Text) node)
          .getChars()
          .toString();
    }

    if (node instanceof StrongEmphasis) {
      return tag("strong", children(node));
    }

    if (node instanceof Emphasis) {
      return tag("em", children(node));
    }

    if (node instanceof Strikethrough) {
      return tag("s", children(node));
    }

    if (node instanceof Link link) {

      Map<String, Object> a = tag("a", children(node));

      Map<String, String> attrs = new HashMap<>();
      attrs.put("href", link
          .getUrl()
          .toString());

      a.put("attrs", attrs);

      return a;
    }

    if (node instanceof Code code) {

      return tag("code", Collections.singletonList(code
          .getText()
          .toString()));
    }

    return null;
  }

  private static List<Object> children(Node parent) {

    List<Object> list = new ArrayList<>();

    for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {

      Object obj = convertInline(child);

      if (obj == null) {
        obj = convertNode(child);
      }

      if (obj != null) {
        list.add(obj);
      }

    }

    return list;
  }

  private static List<Object> listChildren(Node parent) {

    List<Object> list = new ArrayList<>();

    for (Node child = parent.getFirstChild(); child != null; child = child.getNext()) {

      Object node = convertNode(child);

      if (node != null) {
        list.add(node);
      }

    }

    return list;
  }

  private static Map<String, Object> tag(String tag, Object children) {

    Map<String, Object> map = new LinkedHashMap<>();

    map.put("tag", tag);

    if (children != null) {
      map.put("children", children);
    }

    return map;
  }

  private static Object convertTable(TableBlock table) {

    Map<String, Object> tableTag = tag("table", new ArrayList<>());

    List<Object> rows = (List<Object>) tableTag.get("children");

    for (Node part = table.getFirstChild(); part != null; part = part.getNext()) {

      for (Node row = part.getFirstChild(); row != null; row = row.getNext()) {

        Map<String, Object> tr = tag("tr", new ArrayList<>());

        List<Object> cells = (List<Object>) tr.get("children");

        for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {

          cells.add(tag("td", children(cell)));

        }

        rows.add(tr);
      }
    }

    return tableTag;
  }
}