package org.eu.thedoc.zettelnotes.buttons.ocr;

import androidx.annotation.Keep;

public class Tessaract {

  @Keep
  public interface Constants {

    String TESS_DATA_DOWNLOAD_URL = "https://github.com/tesseract-ocr/tessdata_best/raw/4.0.0/%s.traineddata";
    String TESS_DATA_DOWNLOAD_URL_FAST = "https://github.com/tesseract-ocr/tessdata_fast/raw/4.0.0/%s.traineddata";

    String LANGUAGE_CODE = "%s.traineddata";
    String DEFAULT_LANGUAGE = "eng";
    String FOLDER = "tesseract";
    String FOLDER_FAST = "tesseract-fast";
    String DATA_FOLDER = "tessdata";
  }

}
