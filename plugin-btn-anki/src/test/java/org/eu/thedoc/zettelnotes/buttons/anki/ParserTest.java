package org.eu.thedoc.zettelnotes.buttons.anki;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.eu.thedoc.zettelnotes.buttons.anki.Parser.Card;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  public void createCardFromRuled() {
    String content = """
                     Question
                     ---
                     Answer


                     Question 2
                     """;
    List<Card> list = Parser.getCards(content);
    //Assert that two cards are created
    assertEquals(1, list.size());
    //
    assertEquals("Question", list.get(0).question());
    assertEquals("Answer", list.get(0).answer());
  }

  @Test
  public void createCardsFromRuled() {
    String content = """
                     Question
                     ---
                     Answer

                     Question 2
                     ---
                     Answer 2
                     """;
    List<Card> list = Parser.getCards(content);
    //Assert that two cards are created
    assertEquals(2, list.size());
    //
    assertEquals("Question", list.get(0).question());
    assertEquals("Answer", list.get(0).answer());
    //
    assertEquals("Question 2", list.get(1).question());
    assertEquals("Answer 2", list.get(1).answer());
  }

  @Test
  public void createCardFromQA() {
    String content = """
                     Q: Question
                     A: Answer


                     Question 2
                     """;
    List<Card> list = Parser.getCards(content);
    //Assert that two cards are created
    assertEquals(1, list.size());
    //
    assertEquals("Question", list.get(0).question());
    assertEquals("Answer", list.get(0).answer());
  }

  @Test
  public void createCardsFromQA() {
    String content = """
                     Q: Question
                     A: Answer

                     Q: Question 2
                     A: Answer 2
                     """;
    List<Card> list = Parser.getCards(content);
    //Assert that two cards are created
    assertEquals(2, list.size());
    //
    assertEquals("Question", list.get(0).question());
    assertEquals("Answer", list.get(0).answer());
    //
    assertEquals("Question 2", list.get(1).question());
    assertEquals("Answer 2", list.get(1).answer());
  }

  @Test
  public void createCardFromHeader() {
    String content = """
                     # Question
                                          
                     Answer

                     Question 2
                     """;
    List<Card> list = Parser.getCards(content);
    //Assert that two cards are created
    assertEquals(1, list.size());
    //
    assertEquals("Question", list.get(0).question());
    assertEquals("Answer", list.get(0).answer());
  }

  @Test
  public void createCardsFromHeader() {
    String content = """
                     # Question
                                          
                     Answer

                     # Question 2
                                          
                     Answer 2
                     """;
    List<Card> list = Parser.getCards(content);
    //Assert that two cards are created
    assertEquals(2, list.size());
    //
    assertEquals("Question", list.get(0).question());
    assertEquals("Answer", list.get(0).answer());
    //
    assertEquals("Question 2", list.get(1).question());
    assertEquals("Answer 2", list.get(1).answer());
  }

}