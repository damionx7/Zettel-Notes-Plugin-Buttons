package org.eu.thedoc.zettelnotes.buttons.anki;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eu.thedoc.zettelnotes.buttons.anki.Parser.Card;
import org.eu.thedoc.zettelnotes.buttons.anki.Parser.Type;

public class AnkiConfig {

  // Name of deck which will be created in AnkiDroid
  public static final String DECK_NAME = "Zettel Notes (Android)";
  // Name of model which will be created in AnkiDroid
  public static final String MODEL_NAME = "org.eu.thedoc.zettelnotes.buttons.anki";
  // Optional space separated list of tags to add to every note
  public static final Set<String> TAGS = new HashSet<>(Collections.singletonList("None"));
  // List of field names that will be used in AnkiDroid model
  public static final String[] FIELDS = {"Front", "Back"};

  public static ArrayList<HashMap<String, String>> getData(List<Card> list) {
    ArrayList<HashMap<String, String>> data = new ArrayList<>();
    for (int idx = 0; idx < list.size(); idx++) {
      Card card = list.get(idx);
      //Skip Cloze
      if (!card.type().equals(Type.CLOZE)) {
        HashMap<String, String> hm = new HashMap<>();
        hm.put(FIELDS[0], card.question());
        hm.put(FIELDS[1], card.answer());
        data.add(hm);
      }
    }
    return data;
  }

}