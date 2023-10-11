package org.eu.thedoc.zettelnotes.plugins.alarm.database;

public class RecurrenceModel {

  private COOKIE mCOOKIE;
  private int digit;

  public RecurrenceModel(int digit, COOKIE cookie) {
    this.digit = digit;
    mCOOKIE = cookie;
  }

  public COOKIE getCOOKIE() {
    return mCOOKIE;
  }

  public void setCOOKIE(COOKIE COOKIE) {
    mCOOKIE = COOKIE;
  }

  public int getDigit() {
    return digit;
  }

  public void setDigit(int digit) {
    this.digit = digit;
  }

  public enum COOKIE {
    HOUR("h"),
    DAY("d"),
    WEEK("w"),
    MONTH("m"),
    YEAR("y");

    final String constant;

    COOKIE(String constant) {
      this.constant = constant;
    }

    public String getConstant() {
      return this.constant;
    }
  }
}