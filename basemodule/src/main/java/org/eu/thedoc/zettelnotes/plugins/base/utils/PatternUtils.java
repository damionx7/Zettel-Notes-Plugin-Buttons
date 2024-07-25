package org.eu.thedoc.zettelnotes.plugins.base.utils;

import java.util.regex.Pattern;

public class PatternUtils {

  public enum Regex {

    /**
     * [scheduled]: <2023-09-01 09:12 .+1w>
     * <br>
     * Text
     */
    ALARM(Pattern.compile("^\\[scheduled\\]: <(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})(| (\\+\\d+?\\w))>(\\n|)(^.*$|)",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)),
    RECURRENCE(Pattern.compile("(\\+)(\\d+?)(\\w)", Pattern.CASE_INSENSITIVE)),
    /**
     * 0-11	 - [x] task 1. 0-1 2. 4-5	x 3. 7-11	task
     */
    TASK(Pattern.compile("^([ \\t]*)[-*]\\s\\[([\\sxX])\\]\\s(.+)$")),
    //TodoTxt
    TodoTxt(Pattern.compile("((x) |)\\((\\w)\\) ((\\d{4}-\\d{2}-\\d{2}) |)((\\d{4}-\\d{2}-\\d{2}) |)(.+)", Pattern.CASE_INSENSITIVE)),
    //Anki
    /**
     * # Style
     * <br>
     * This style is suitable for having the header as the front, and the answer as the back
     */
    ANKI_HEADER(Pattern.compile("^# (.+?)\\n+([^#]*)\\n{2,}")),
    /**
     * Q: How do you use this style?
     * <br>
     * A: Just like this.
     */
    ANKI_QA(Pattern.compile("^Q: (.+?)\\n+A: (.+?)$")),
    /**
     * How do you use ruled style?
     * <br>
     * ---
     * <br>
     * You need at least three '-' between the front and back of the card.
     */
    ANKI_RULED(Pattern.compile("(.+?)\\n-{3,}\\n(.+?)$")),
    /**
     * The idea of {cloze paragraph style} is to be able to recognise any paragraphs that contain {cloze deletions}.
     */
    ANKI_CLOZE(Pattern.compile("^(.*?)\\{(.+?)\\}(.*?)$")),
    /**
     * Find cloze only
     */
    ANKI_CLOZE_ONLY(Pattern.compile("\\{(.+?)\\}"));

    public final Pattern pattern;
    public final String regex;

    Regex(Pattern pattern) {
      this.pattern = pattern;
      regex = pattern.pattern();
    }

  }
}