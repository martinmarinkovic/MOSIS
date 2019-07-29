package com.martinmarinkovic.partytime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static int NEW_PLACE = -1;
    public static final int SHOW_MAP = 0; //prikaz trenutne korisnikove lokacije
    public static final int CENTER_PLACE_ON_MAP = 1; //Zumirati na koordinate prosledjene lokacije
    public static final int SELECT_COORDINATES = 2; //Omoguciti klik na mapu zarad odabira koordinata
    private int state = 0;
    private boolean selCoorsEnabled = false;
    private LatLng placeLoc;
    private HashMap<Marker, Integer> markerPlaceIdMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        placeLoc = new LatLng(43.32103776032881, 21.895688250660893); // Nis

        try {
            Intent mapIntent = getIntent();
            Bundle mapBundle = mapIntent.getExtras();
            if (mapBundle != null) {
                state = mapBundle.getInt("state");

                if (state == CENTER_PLACE_ON_MAP) {
                    String placeLat = mapBundle.getString("lat");
                    String placeLon = mapBundle.getString("lon");
                    placeLoc = new LatLng(Double.parseDouble(placeLat), Double.parseDouble(placeLon));
                }
            }
        } catch (Exception e) {
            Log.d("Error", "Error reading state");
        }

        setContentView(R.layout.activity_google_maps); // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GoogleMapsActivity.this, AddNewPlace.class);
                startActivity(intent);
            }
        });

        /*
        if (state != SELECT_COORDINATES) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(GoogleMapsActivity.this, MainActivity.class);
                    startActivityForResult(i, NEW_PLACE);
                }
            });
        } else {
            ViewGroup layout = (ViewGroup) fab.getParent();
            if (layout != null) {
                layout.removeView(fab);
            }
        }*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            if (state == SHOW_MAP) {  //korisnikova lokacija???
                mMap.setMyLocationEnabled(true);
                if (placeLoc != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                }
            } else if (state == SELECT_COORDINATES) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                selCoorsEnabled = true;
                setOnMapClickListener();
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
            }
            addMyPlaceMarkers();
        }
    }

    private void setOnMapClickListener () {
        if (mMap != null) {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (state == SELECT_COORDINATES && selCoorsEnabled) {
                        String lon = Double.toString(latLng.longitude);
                        String lat = Double.toString(latLng.latitude);
                        Intent locationIntent = new Intent();
                        locationIntent.putExtra("lon", lon);
                        locationIntent.putExtra("lat", lat);
                        setResult(Activity.RESULT_OK, locationIntent);
                        finish();
                    }
                }
            });
        }
    }

    @SuppressLint("MissingPermission")

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mMap.setMyLocationEnabled(true);
//                    setOnMapClickListener();
                    if (state == SHOW_MAP) {
                        mMap.setMyLocationEnabled(true);
                        if (placeLoc != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                        }
                    } else if (state == CENTER_PLACE_ON_MAP) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                        setOnMapClickListener();//?????????????????
                        //treba da zmiramo na to konkretno mesto
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
                    }
                    addMyPlaceMarkers();
                }
                return;
            }
        }
    }

    private void addMyPlaceMarkers () {
        ArrayList<MyPlace> places = MyPlacesData.getInstance().getMyPlaces();
        markerPlaceIdMap = new HashMap<Marker, Integer>((int) ((double) places.size() * 1.2));
        for (int i = 0; i < places.size(); i++) {
            MyPlace place = places.get(i);
            String lat = place.getLatitude();
            String lon = place.getLongitude();
            LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(loc);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.sharp_add_black_36));
            markerOptions.title(place.getName());
            Marker marker = mMap.addMarker(markerOptions);
            markerPlaceIdMap.put(marker, i);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int i  = markerPlaceIdMap.get(marker);
                Bundle positionBundle = new Bundle();
                positionBundle.putInt("position", i);
                positionBundle.putInt("activity", 1);
                Intent intent = new Intent(GoogleMapsActivity.this, PlaceProfile.class);
                intent.putExtras(positionBundle);
                startActivity(intent);
                return false;
            }
        });
    }


}
