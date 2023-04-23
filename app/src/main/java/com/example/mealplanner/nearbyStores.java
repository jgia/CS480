package com.example.mealplanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class nearbyStores extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearbystores);
       listView = findViewById(R.id.listview);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        addGroceryStoreMarkers();

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));

            String text = "My current location is: " +
                    "Latitude = " + loc.getLatitude() +
                    "Longitude = " + loc.getLongitude();


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
    private void addGroceryStoreMarkers() {
        LatLng starMarket = new LatLng(42.391825, -71.178724);
        mMap.addMarker(new MarkerOptions()
                .position(starMarket)
                .title("Star Market")
                .snippet("33 Kilmarnock St, Boston, MA 02215"));

        LatLng russo = new LatLng(42.365793, -71.183954);
        mMap.addMarker(new MarkerOptions()
                .position(russo)
                .title("Russo's")
                .snippet("560 Pleasant St, Watertown, MA 02472"));

        LatLng wholeFoods = new LatLng(42.318463, -71.209991);
        mMap.addMarker(new MarkerOptions()
                .position(wholeFoods)
                .title("Whole Foods Market")
                .snippet("15 Westland Ave, Boston, MA 02115"));

        LatLng stopAndShop = new LatLng(42.364019, -71.183214);
        mMap.addMarker(new MarkerOptions()
                .position(stopAndShop)
                .title("Stop & Shop")
                .snippet("1391 Beacon St, Brookline, MA 02446"));

        LatLng traderJoes = new LatLng(42.313490, -71.194168);
        mMap.addMarker(new MarkerOptions()
                .position(traderJoes)
                .title("Trader Joe's")
                .snippet("1317 Beacon St, Brookline, MA 02446"));

        String[] groceryStoreArray = {
                "Star Market 33 Kilmarnock St, Boston, MA 02215",
                "Russo's 560 Pleasant St, Watertown, MA 02472",
                "Whole Foods Market 15 Westland Ave, Boston, MA 02115",
                "Stop & Shop 1391 Beacon St, Brookline, MA 02446",
                "Trader Joe's 1317 Beacon St, Brookline, MA 02446"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groceryStoreArray);
        listView.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
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
