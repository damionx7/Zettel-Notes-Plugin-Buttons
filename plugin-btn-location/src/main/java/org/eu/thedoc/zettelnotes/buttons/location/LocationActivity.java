package org.eu.thedoc.zettelnotes.buttons.location;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//Dummy activity for intent filter
public class LocationActivity extends AppCompatActivity {

  public static final String INTENT_EXTRA_LOCATION = "intent-extra-location";
  public static final String ERROR_STRING = "intent-error";
  private static final String DEFAULT_LOC_STRING = "[Location at $loctime$](https://maps.google.com/maps?q=$lat$,$long$)";
  //private static final String DEFAULT_LOC_STRING = "![map](latlong:$lat$,$long$)";

  private ProgressBar mProgressBar;

  private String time2humanFromLastModified (Long time2convert) {
    try {
      String pattern = "dd MMM yyyy hh:mm:ss a";
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
      return simpleDateFormat.format(new Date(time2convert));
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  protected void onCreate (@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location);
    mProgressBar = findViewById(R.id.progressBar);
    mProgressBar.setIndeterminate(true);

    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
    boolean enabled = sharedPreferences.getBoolean("prefs_enable", true);
    if(!enabled) {
      setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Plugin disabled"));
      showToast("Error: Plugin disabled");
      finish();
    }else {
      ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(ACCESS_COARSE_LOCATION, false);
        if (fineLocationGranted != null && fineLocationGranted) {
          // Precise location access granted.
          sendLastLocation();
        } else if (coarseLocationGranted != null && coarseLocationGranted) {
          // Only approximate location access granted.
          sendLastLocation();
        } else {
          // No location access granted.
          setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "No location access granted"));
          showToast("Error: No location access granted");
          finish();
        }
      });
      if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
          ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        sendLastLocation();
      } else {
        locationPermissionRequest.launch(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION});
      }
    }
  }

  @Override
  protected void onStop () {
    super.onStop();
    if(mProgressBar != null) mProgressBar.setVisibility(View.GONE);
  }

  @SuppressLint("MissingPermission")
  private void sendLastLocation () {
    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
      // Got last known location. In some rare situations this can be null.
      if (location != null) {
        setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_LOCATION, computeLocationString(location)));
        finish();
      } else {
        if(!isLocationEnabled()) {
          setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, "Location disabled"));
          showToast("Error: Location disabled");
          finish();
        } else {
          mProgressBar.setVisibility(View.VISIBLE);
          fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(location1 -> {
            setResult(RESULT_OK, new Intent().putExtra(INTENT_EXTRA_LOCATION, computeLocationString(location1)));
            finish();
          }).addOnFailureListener(e -> {
            setResult(RESULT_CANCELED, new Intent().putExtra(ERROR_STRING, e.toString()));
            finish();
          });
        }
      }
    });
  }

  private boolean isLocationEnabled () {
    //exceptions will be thrown if provider is not permitted.
    var locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    boolean gps_enabled = false, network_enabled = false;
    try{ gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ignored){}
    try{ network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ignored){}
    return gps_enabled && network_enabled;
  }

  private String computeLocationString (Location location) {
    if(location == null) return "";
    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
    String userFormat = sharedPreferences.getString("prefs_location_link_format", DEFAULT_LOC_STRING);
    if (userFormat.isEmpty()) userFormat = DEFAULT_LOC_STRING;

    String locTime = time2humanFromLastModified(location.getTime());
    String longitude = String.valueOf(location.getLongitude());
    String latitude = String.valueOf(location.getLatitude());
    String accuracy = String.valueOf(location.getAccuracy());
    String altitude = String.valueOf(location.getAltitude());
    String speed = String.valueOf(location.getSpeed());
    userFormat = userFormat.replaceAll("\\$loctime\\$", locTime)
                           .replaceAll("\\$long\\$", longitude)
                           .replaceAll("\\$lat\\$", latitude)
                           .replaceAll("\\$acc\\$", accuracy)
                           .replaceAll("\\$alt\\$", altitude)
                           .replaceAll("\\$speed\\$", speed);
    return userFormat;
  }

  private void showToast (String text) {
    if (!text.isEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }


}
