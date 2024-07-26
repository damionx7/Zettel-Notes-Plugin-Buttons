package org.eu.thedoc.zettelnotes.buttons.anki;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eu.thedoc.zettelnotes.buttons.anki.Parser.Card;
import org.eu.thedoc.zettelnotes.plugins.base.utils.ToastsHelper;

public class MainActivity
    extends Activity {

  public static final int REQ_CODE = 0;
  public static final String EXTRA_TEXT_SELECTED = "intent-extra-text-selected";
  public static final String ERROR_STRING = "intent-error";

  private Anki mAnki;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_launcher);
    // Create instance of helper class
    mAnki = new Anki(this);
    //Check For Permission
    if (mAnki.shouldRequestPermission()) {
      mAnki.requestPermission(REQ_CODE);
    } else {
      //Handle Text
      handleText(getText());
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQ_CODE) {
      if (permissions.length == 0) {
        return;
      }
      String permission = permissions[0];
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //permission granted . now Handle Text
        handleText(getText());
      } else {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
          //permission declined
        } else {
          //permission declined don't ask again
        }
      }
    }
  }

  private String getText() {
    return getIntent().getStringExtra(EXTRA_TEXT_SELECTED);
  }

  private void handleText(String text) {
    if (Anki.isApiAvailable(this)) {
      //Get Cards From Text
      List<Card> list = Parser.getCards(text);
      ArrayList<HashMap<String, String>> basicQA = AnkiConfig.getData(list);
      //ArrayList<HashMap<String, String>> cloze = AnkiConfig.getData(list);
      Log.v(getClass().getName(), "Data Size: " + basicQA.size());
      //
      Long deckId = mAnki.getDeckId();
      Long modelId = mAnki.getModelId();
      if ((deckId == null) || (modelId == null)) {
        // we had an API error, report failure and return
        onFailure("Api Error. Deck or Model Null.");
        return;
      }
      String[] fieldNames = mAnki.getApi().getFieldList(modelId);
      if (fieldNames == null) {
        // we had an API error, report failure and return
        onFailure("Api Error. Field Null.");
        return;
      }
      // Build list of fields and tags
      LinkedList<String[]> fields = new LinkedList<>();
      LinkedList<Set<String>> tags = new LinkedList<>();
      for (Map<String, String> fieldMap : basicQA) {
        // Build a field map accounting for the fact that the user could have changed the fields in the model
        String[] flds = new String[fieldNames.length];
        for (int i = 0; i < flds.length; i++) {
          // Fill up the fields one-by-one until either all fields are filled or we run out of fields to send
          if (i < AnkiConfig.FIELDS.length) {
            flds[i] = fieldMap.get(AnkiConfig.FIELDS[i]);
          }
        }
        tags.add(AnkiConfig.TAGS);
        fields.add(flds);
      }
      int added = mAnki.getApi().addNotes(modelId, deckId, fields, tags);
      if (added != 0) {
        ToastsHelper.showToast(this, "Success. Added " + added + " Notes.");
        onSuccess();
      } else {
        // API indicates that a 0 return value is an error
        onFailure("Api Error. Can't Add Notes.");
      }
    } else {
      onFailure("Anki Api Not Available. Enable in Anki > Settings > Advanced > Enable API.");
    }
  }

  private void onSuccess() {
    setResult(RESULT_OK);
    finish();
  }

  private void onFailure(String message) {
    ToastsHelper.showToast(this, message);
    setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Select some text"));
    finish();
  }

}