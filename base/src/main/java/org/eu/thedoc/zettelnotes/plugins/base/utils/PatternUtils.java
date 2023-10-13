package org.eu.thedoc.zettelnotes.plugins.base.utils;

import java.util.regex.Pattern;

public class PatternUtils {

  public enum Regex {

    // [scheduled]: <2023-09-01 09:12 .+1w>
    ALARM(Pattern.compile("\\[scheduled\\]: <(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})(| (\\+\\d+?\\w))>(\\n|)(^.*$|)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)),
    RECURRENCE(Pattern.compile("(\\+)(\\d+?)(\\w)", Pattern.CASE_INSENSITIVE)),
    //0-11	 - [x] task
    //1. 0-1
    //2. 4-5	x
    //3. 7-11	task
    TASK(Pattern.compile("^([ \\t]*)[-*]\\s\\[([\\sxX])\\]\\s(.+)$"));

    public final Pattern pattern;
    public final String regex;

    Regex(Pattern pattern) {
      this.pattern = pattern;
      regex = pattern.pattern();
    }

  }
}
