package org.eu.thedoc.zettelnotes.buttons.anki;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import org.eu.thedoc.zettelnotes.plugins.base.utils.PatternUtils.Regex;

public class Parser {

  @NonNull
  public static List<Card> getCards(String text) {
    if (text == null || text.isEmpty()) {
      return new ArrayList<>();
    }

    Set<Card> set = new HashSet<>();
    set.addAll(getCloze(text));
    set.addAll(getRuled(text));
    set.addAll(getQA(text));
    set.addAll(getHeader(text));
    return new ArrayList<>(set);
  }

  private static List<Card> getCloze(String text) {
    List<Card> list = new ArrayList<>();
    Matcher clozeMatcher = Regex.ANKI_CLOZE.pattern.matcher(text);
    while (clozeMatcher.find()) {
      //Get all lines with Cloze
      String group = clozeMatcher.group();
      //Convert to Anki Format
      Matcher matcher = Regex.ANKI_CLOZE_ONLY.pattern.matcher(group);
      StringBuffer stringBuffer = new StringBuffer();
      int index = 0;
      while (matcher.find()) {
        matcher.appendReplacement(stringBuffer, String.format("{{c%s:%s}}", index, matcher.group(1)));
        index++;
      }
      matcher.appendTail(stringBuffer);
      //Add in list
      String card = stringBuffer.toString();
      list.add(new Card(card, "", Type.CLOZE));
    }
    return list;
  }

  private static List<Card> getRuled(String text) {
    List<Card> list = new ArrayList<>();
    Matcher clozeMatcher = Regex.ANKI_RULED.pattern.matcher(text);
    while (clozeMatcher.find()) {
      //Get all lines with ruled format
      String group1 = clozeMatcher.group(1);
      String group2 = clozeMatcher.group(2);
      //Add in list
      list.add(new Card(group1, group2, Type.RULED));
    }
    return list;
  }

  private static List<Card> getQA(String text) {
    List<Card> list = new ArrayList<>();
    Matcher clozeMatcher = Regex.ANKI_QA.pattern.matcher(text);
    while (clozeMatcher.find()) {
      //Get all lines with ruled format
      String group1 = clozeMatcher.group(1);
      String group2 = clozeMatcher.group(2);
      //Add in list
      list.add(new Card(group1, group2, Type.QA));
    }
    return list;
  }

  private static List<Card> getHeader(String text) {
    List<Card> list = new ArrayList<>();
    Matcher clozeMatcher = Regex.ANKI_HEADER.pattern.matcher(text);
    while (clozeMatcher.find()) {
      //Get all lines with ruled format
      String group1 = clozeMatcher.group(1);
      String group2 = clozeMatcher.group(2);
      //Add in list
      list.add(new Card(group1, group2, Type.HEADER));
    }
    return list;
  }

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({Type.CLOZE, Type.HEADER, Type.QA, Type.RULED})
  public @interface Type {

    String CLOZE = "cloze";
    String HEADER = "header";
    String RULED = "ruled";
    String QA = "qa";
  }

  public record Card(String question, String answer, @Type String type) {}
}