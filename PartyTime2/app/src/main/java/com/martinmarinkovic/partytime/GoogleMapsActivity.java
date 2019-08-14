package com.martinmarinkovic.partytime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    private static final float DEFAULT_ZOOM = 15f;
    private EditText mSearchText;
    private List<MyPlace> mPlacesList, lista;
    private ListView myPlacesListView;
    private ImageView searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_google_maps);
        mSearchText = (EditText) findViewById(R.id.input_search);
        searchBtn = (ImageView) findViewById(R.id.ic_magnify);
        mSearchText = (EditText) findViewById(R.id.input_search);
        lista = MyPlacesData.getInstance().getMyPlaces();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapsActivity.this, SearchPlaces.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab_add_new_place = (FloatingActionButton) findViewById(R.id.fab_add_new_place);
        fab_add_new_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GoogleMapsActivity.this, AddNewPlace.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab_user_location = (FloatingActionButton) findViewById(R.id.fab_get_user_location);
        fab_user_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserLocation();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLoc,mMap.getMaxZoomLevel() - 5));
                }
            }
        } catch (Exception e) {
            Log.d("Error", "Error reading state");
        }

        hideSoftKeyboard();
        initTextListener();
    }

    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

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

        //hideSoftKeyboard();
        //initTextListener();
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
            String type = place.getType();
            if(type.equals("Club")) {
                markerOptions.position(loc);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_ic_club));
                markerOptions.title(place.getName());
                Marker marker = mMap.addMarker(markerOptions);
                markerPlaceIdMap.put(marker, i);
            } else if(type.equals("Tavern")) {
                markerOptions.position(loc);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_ic_tavern));
                markerOptions.title(place.getName());
                Marker marker = mMap.addMarker(markerOptions);
                markerPlaceIdMap.put(marker, i);
            } else if(type.equals("Beer House")) {
                markerOptions.position(loc);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_ic_beer_house));
                markerOptions.title(place.getName());
                Marker marker = mMap.addMarker(markerOptions);
                markerPlaceIdMap.put(marker, i);
            }
            else if(type.equals("Private")) {
                markerOptions.position(loc);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_ic_private_party));
                markerOptions.title(place.getName());
                Marker marker = mMap.addMarker(markerOptions);
                markerPlaceIdMap.put(marker, i);
            }
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

    private void getUserLocation() {
        //mMap.setMyLocationEnabled(true);
        Location userLocation = mMap.getMyLocation();
        LatLng myLocation = null;
        if (userLocation != null) {
            myLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,mMap.getMaxZoomLevel() - 5));
        }
    }

    private void setList(){
        myPlacesListView = (ListView)findViewById(R.id.listView);
        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, MyPlacesData.getInstance().getMyPlaces()));
    }

    private void initTextListener(){

        mPlacesList = new ArrayList<>();
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchText.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword){

        mPlacesList.clear();
        if(keyword.length() ==0){
        }else{
            setList();
            for(MyPlace myPlace : lista){
                if(myPlace.getName().toLowerCase().contains(keyword.toLowerCase())){
                    mPlacesList.add(myPlace);
                    updateSearchList();
                }
            }
        }
    }

    private void updateSearchList(){

        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
        myPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(GoogleMapsActivity.this,""+ mPlacesList.get(position).getName(), Toast.LENGTH_SHORT).show();
                mSearchText.setText(mPlacesList.get(position).getName());
                geoLocate(mPlacesList.get(position));
            }
        });
    }

    private void geoLocate(MyPlace myPlace){
        LatLng placeLoc = new LatLng(Double.parseDouble(myPlace.getLatitude()), Double.parseDouble(myPlace.getLongitude()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLoc,mMap.getMaxZoomLevel() - 5));
        mPlacesList.clear();
        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showAllUsersLocation() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("Users");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    double latitude = ds.child("latitude").getValue(Double.class);
                    double longitude = ds.child("longitude").getValue(Double.class);
                    Log.d("TAG", latitude + " / " +  longitude);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        usersRef.addListenerForSingleValueEvent(eventListener);
    }


}
