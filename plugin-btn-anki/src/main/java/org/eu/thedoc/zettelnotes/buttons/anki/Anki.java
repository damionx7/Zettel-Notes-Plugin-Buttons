package org.eu.thedoc.zettelnotes.buttons.anki;

import static com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.ichi2.anki.api.AddContentApi;
import java.util.Map;

public class Anki {

  private final AddContentApi mApi;
  private final Activity mContext;

  public Anki(Activity context) {
    mContext = context;
    mApi = new AddContentApi(context);
  }

  /**
   * Whether or not the API is available to use. The API could be unavailable if AnkiDroid is not installed or the user explicitly disabled
   * the API
   *
   * @return true if the API is available to use
   */
  public static boolean isApiAvailable(Context context) {
    return AddContentApi.getAnkiDroidPackageName(context) != null;
  }

  public AddContentApi getApi() {
    return mApi;
  }

  /**
   * Whether or not we should request full access to the AnkiDroid API
   */
  public boolean shouldRequestPermission() {
    return ContextCompat.checkSelfPermission(mContext, READ_WRITE_PERMISSION) != PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Get the ID of the deck which matches the name
   *
   * @param deckName Exact name of deck (note: deck names are unique in Anki)
   * @return the ID of the deck that has given name, or null if no deck was found or API error
   */
  private Long findDeckId(String deckName) {
    Map<Long, String> deckList = mApi.getDeckList();
    if (deckList != null) {
      for (Map.Entry<Long, String> entry : deckList.entrySet()) {
        if (entry.getValue().equalsIgnoreCase(deckName)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  /**
   * get the deck id
   *
   * @return might be null if there was a problem
   */
  public Long getDeckId() {
    Long did = findDeckId(AnkiConfig.DECK_NAME);
    if (did == null) {
      did = getApi().addNewDeck(AnkiConfig.DECK_NAME);
    }
    return did;
  }

  /**
   * get model id
   *
   * @return might be null if there was an error
   */
  public Long getModelId() {
    Long mid = findModelId(AnkiConfig.MODEL_NAME, AnkiConfig.FIELDS.length);
    if (mid == null) {
      mid = getApi().addNewBasicModel(AnkiConfig.MODEL_NAME);
    }
    return mid;
  }


  /**
   * Try to find the given model by name, accounting for renaming of the model: If there's a model with this modelName that is known to have
   * previously been created (by this app) and the corresponding model ID exists and has the required number of fields then return that ID
   * (even though it may have since been renamed) If there's a model from #getModelList with modelName and required number of fields then
   * return its ID Otherwise return null
   *
   * @param modelName the name of the model to find
   * @param numFields the minimum number of fields the model is required to have
   * @return the model ID or null if something went wrong
   */
  private Long findModelId(String modelName, int numFields) {
    Map<Long, String> modelList = mApi.getModelList(numFields);
    if (modelList != null) {
      for (Map.Entry<Long, String> entry : modelList.entrySet()) {
        if (entry.getValue().equals(modelName)) {
          return entry.getKey(); // first model wins
        }
      }
    }
    return null;
  }

  public void requestPermission(int reqCode) {
    ActivityCompat.requestPermissions(mContext, new String[]{READ_WRITE_PERMISSION}, reqCode);
  }
}