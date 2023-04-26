package com.example.mealplanner;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class nearbyStores extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 0;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;
    private Marker selectedMarker;
    private EditText editText;
    ArrayList<String> locationList = new ArrayList<>();
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearbystores);

        text = findViewById(R.id.text_view);
        Button button = findViewById(R.id.button_go);
        editText = findViewById(R.id.searchbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //Running a location check in the oncreate to create values of lat and long for lookup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();

        }
        //Autocomplete implementation
        editText.setFocusable(false);
        editText.setOnClickListener(v -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS
                    , Place.Field.LAT_LNG, Place.Field.NAME);
            //intent
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(nearbyStores.this);
            //Send intent to complete search for new address
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        //Opens google maps. Route is centered marker to selected marker.
        button.setOnClickListener(v -> {
            if (selectedMarker != null) {
                LatLng latLng = selectedMarker.getPosition();

                // create an Intent to launch Google Maps
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" +
                                latitude + "," + longitude + "&destination=" +
                                latLng.latitude + "," + latLng.longitude));
                startActivity(intent);
            } else {
                Toast.makeText(nearbyStores.this, "Please select a marker", Toast.LENGTH_SHORT).show();
            }
        });

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        //Thread for lookup
        Thread t = new Thread(background);
        t.start();
    }

    //Pause update of current location after initial so it doesn't center map when app is being used.
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    Runnable background = new Runnable() {
        public void run() {

            StringBuilder builder = new StringBuilder();
            // Set the base URL
            String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            // Set the parameters
            String lati = Double.toString(latitude);
            String longi = Double.toString(longitude);
            String location = lati + "%2C" + longi;
            int radius = 90000;
            String type = "supermarket";
            String apiKey = "AIzaSyCiI84UB2NcYD3ESP8u-b6IN-eKiBjETEY";
            // Construct the URL
            String Url = baseUrl + "location=" + location +
                    "&radius=" + radius +
                    "&type=" + type +
                    "&key=" + apiKey;
            InputStream is = null;
            try {
                URL url = new URL(Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("JSON", "The response is: " + response);
                //if response code not 200, end thread
                if (response != 200) return;
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (IOException e) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }

            //convert StringBuilder to String
            String readJSONFeed = builder.toString();
            Log.e("JSON", readJSONFeed);

            //decode JSON
            try {
                JSONObject obj = new JSONObject(readJSONFeed);
                JSONArray resultsArray = obj.getJSONArray("results");


                //for each array item get title and date
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject jsnObject = resultsArray.getJSONObject(i);
                    JSONObject locationObject = jsnObject.getJSONObject("geometry").getJSONObject("location");
                    JSONObject nameObject = resultsArray.getJSONObject(i);
                    String lat = locationObject.getString("lat");
                    String lng = locationObject.getString("lng");
                    String name = nameObject.getString("name");
                    String address = nameObject.getString("vicinity");
                    Log.i("JSON", "Name " + name);
                    //Create marker data
                    LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(name);
                    markerOptions.position(latLng);
                    markerOptions.snippet(address);
                    String nameAndAddress = name + ", " + address;
                    locationList.add(nameAndAddress);

                    //add markers on UI Thread
                    runOnUiThread(() -> mMap.addMarker(markerOptions));
                }
            } catch (JSONException e) {
                e.getMessage();
                e.printStackTrace();
            }
        }
    };

    //Activity result after user chooses option from autocomplete menu.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                editText.setText(place.getAddress());
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    //Adding marker and aligning maps with it for new location
                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(place.getAddress())
                            .snippet("Lookup Location");
                    mMap.addMarker(options);
                    //Centers map around new location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;
                    //Restarts the threat to update the map using PLACESAPI
                    Thread t = new Thread(background);
                    t.start();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();

        // set up the marker click listener
        // Has to be set up in onMapReady function because markers are set in a background thread.
        // Or at least I think. I tried to put this else where both in the oncreate and as a general function
        // and it didn't work.
        mMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;
            String name = marker.getTitle();
            String address = marker.getSnippet();

            double distance = calculateDistance(marker.getPosition().latitude, marker.getPosition().longitude);
            String distanceString = String.format("%.2f", distance);
            text.setText(name + "\n" + address + " (" + distanceString + " miles away)");

            return false;
        });
    }

    //Calculate distance between middle marker and selected marker.
    public double calculateDistance(double lat2, double lon2) {
        double earthRadius = 3958.8;
        double lat1 = latitude;
        double lon1 = longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000000, 0, locationListener);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();

            LatLng currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),
                    "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(),
                    "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        }

    } // End MyLocationListener

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permission denied. Location access is required for this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.browse:
                startActivity(new Intent(this, searchMeals.class));
                return true;
            case R.id.shopping:
                startActivity(new Intent(this, viewShoppingList.class));
                return true;
            case R.id.nearby:
                startActivity(new Intent(this, nearbyStores.class));
                return true;
            case R.id.home:
                // Handle click on hidden item
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}