package org.eu.thedoc.zettelnotes.buttons.mermaid;

import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

/**
 * Builds mermaid.ink image links. The service takes the diagram as zlib-deflated JSON, URL-safe base64, behind a "pako:" prefix.
 */
public class MermaidInk {

  private static final String BASE_URL = "https://mermaid.ink/img/pako:";
  private static final String IMAGE_PARAMS = "?type=png";
  private static final Pattern FENCE = Pattern.compile("^\\s*```+\\s*mermaid[^\\n]*\\n(.*?)\\n?```+\\s*$", Pattern.DOTALL);

  /**
   * Returns the markdown image for the given diagram code, e.g. ![Mermaid diagram](https://mermaid.ink/img/pako:...?type=png).
   */
  public static String toImageMarkdown(String diagram) {
    return "![Mermaid diagram](" + toImageUrl(diagram) + ")\n";
  }

  public static String toImageUrl(String diagram) {
    String code = stripFence(diagram);
    String state = "{\"code\":\"" + escapeJson(code) + "\",\"mermaid\":\"{\\n  \\\"theme\\\": \\\"default\\\"\\n}\"}";
    byte[] deflated = deflate(state.getBytes(StandardCharsets.UTF_8));
    String encoded = Base64.encodeToString(deflated, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    return BASE_URL + encoded + IMAGE_PARAMS;
  }

  /**
   * Lets the user select a whole fenced block as well as just its body.
   */
  static String stripFence(String text) {
    Matcher matcher = FENCE.matcher(text);
    return (matcher.matches() ? matcher.group(1) : text).trim();
  }

  private static byte[] deflate(byte[] input) {
    Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
    try {
      deflater.setInput(input);
      deflater.finish();
      ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
      byte[] buffer = new byte[1024];
      while (!deflater.finished()) {
        out.write(buffer, 0, deflater.deflate(buffer));
      }
      return out.toByteArray();
    } finally {
      deflater.end();
    }
  }

  private static String escapeJson(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 16);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

}
