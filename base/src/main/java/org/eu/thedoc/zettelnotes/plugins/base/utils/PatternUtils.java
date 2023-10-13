package org.eu.thedoc.zettelnotes.plugins.base.utils;

import java.util.regex.Pattern;

public class PatternUtils {

  public enum Regex {

    // [scheduled]: <2023-09-01 09:12 .+1w>
    ALARM(Pattern.compile("\\[scheduled\\]: <(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})(| (\\+\\d+?\\w))>(\\n|)(^.*$|)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)),
    RECURRENCE(Pattern.compile("(\\+)(\\d+?)(\\w)", Pattern.CASE_INSENSITIVE));

    public final Pattern pattern;
    public final String regex;

    Regex(Pattern pattern) {
      this.pattern = pattern;
      regex = pattern.pattern();
    }

  }
}
