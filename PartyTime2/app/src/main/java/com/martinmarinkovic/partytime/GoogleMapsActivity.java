package com.martinmarinkovic.partytime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "GoogleMaps";
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
    private  String searchRadius, searchType;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<UserLocation> locations;
    private UserLocation mUserPosition;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private DatabaseReference mRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_google_maps);
        mSearchText = (EditText) findViewById(R.id.input_search);
        searchBtn = (ImageView) findViewById(R.id.select_catgory);
        mSearchText = (EditText) findViewById(R.id.input_search);
        lista = MyPlacesData.getInstance().getMyPlaces();
        mPlacesList = new ArrayList<>();
        mRef = FirebaseDatabase.getInstance().getReference();
        locations = new ArrayList<>();

        if (mUserLocations.size() == 0)
            mUserLocations = UserData.getInstance().getUserLocationsList();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapsActivity.this, SearchByAtributes.class);
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

        try {
            Intent searchIntent = getIntent();
            Bundle searchBundle = searchIntent.getExtras();
            if (searchBundle != null) {
                searchRadius = searchBundle.getString("radius");
                searchType = searchBundle.getString("type");
            }
        } catch (Exception e) {
            Log.d("Error", "Error reading state");
        }

        if (searchType == null)
            searchType = "All";

        if (searchRadius == null)
            searchRadius = " None ";

        hideSoftKeyboard();
        initTextListener();
    }

    /*
        @Override
        public void onResume() {
            super.onResume();
            startUserLocationsRunnable(); // update user locations every 'LOCATION_UPDATE_INTERVAL'
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onStop() {
            super.onStop();
        }
    */
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
            addMapMarkers();
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
                try {
                    int i = markerPlaceIdMap.get(marker);
                    Bundle positionBundle = new Bundle();
                    positionBundle.putInt("position", i);
                    positionBundle.putInt("activity", 1);
                    Intent intent = new Intent(GoogleMapsActivity.this, PlaceProfile.class);
                    intent.putExtras(positionBundle);
                    startActivity(intent);
                    return false;
                }catch (Exception e){}
                return false;
            }
        });
    }

    //Na odredjen vremenski interval zovemo retrieveUserLocations();
    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try {
            for (final ClusterMarker clusterMarker : mClusterMarkers) {

                Query query = mRef
                        .child("User Locations")
                        .orderByKey()
                        .equalTo(clusterMarker.getUser().getUserID());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final UserLocation updatedUserLocation = dataSnapshot.getValue(UserLocation.class);

                        // update the location
                        for (int i = 0; i < mClusterMarkers.size(); i++) {
                            try {
                                if (mClusterMarkers.get(i).getUser().getUserID().equals(updatedUserLocation.getUser().getUserID())) {

                                    LatLng updatedLatLng = new LatLng(
                                            updatedUserLocation.getGeo_point().getLatitude(),
                                            updatedUserLocation.getGeo_point().getLongitude()
                                    );

                                    mClusterMarkers.get(i).setPosition(updatedLatLng);
                                    mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                }
                            }
                            catch (Exception e){}
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        catch (Exception e){}
    }

    private void addMapMarkers(){

        if(mMap != null){

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);
            }

            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        GoogleMapsActivity.this,
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for(UserLocation userLocation: mUserLocations){

                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try{
                    String snippet = "";
                    if(userLocation.getUser().getUserID().equals(FirebaseAuth.getInstance().getUid())){
                        snippet = "This is you";
                    }
                    else{
                        snippet = "This is your friend ";
                    }

                    String avatar = String.valueOf(R.drawable.default_avatar); // set the default avatar
                    try{
                        avatar = userLocation.getUser().image;
                    }catch (NumberFormatException e){}

                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeo_point().getLatitude(), userLocation.getGeo_point().getLongitude()),
                            userLocation.getUser().getUsername(),
                            snippet,
                            avatar,
                            userLocation.getUser()
                    );
                    mClusterManager.addItem(newClusterMarker); //Nema bas dobre metode pa kreiramo i donju listu
                    mClusterMarkers.add(newClusterMarker); //Ne dodaje ga odmah na mapu, cuvamo ga kao referencu za kasnije

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }
            }
            mClusterManager.cluster();

            mClusterManager.setOnClusterClickListener(
                    new ClusterManager.OnClusterClickListener<ClusterMarker>() {
                        @Override
                        public boolean onClusterClick(Cluster<ClusterMarker> cluster) {

                            Toast.makeText(GoogleMapsActivity.this, "Cluster click", Toast.LENGTH_SHORT).show();

                            return false;
                        }
                    });

                    mClusterManager.setOnClusterItemClickListener(
                            new ClusterManager.OnClusterItemClickListener<ClusterMarker>() {
                                @Override
                                public boolean onClusterItemClick(ClusterMarker clusterItem) {

                                    String userID = clusterItem.getUser().getUserID();
                                    Bundle positionBundle = new Bundle();
                                    positionBundle.putString("userID", userID);
                                    positionBundle.putString("activity", "FriendsList");
                                    Intent intent = new Intent(GoogleMapsActivity.this, UserProfile.class);
                                    intent.putExtras(positionBundle);
                                    startActivity(intent);
                                    return false;
                                }
                            });

            mClusterManager.setOnClusterItemInfoWindowClickListener(
                    new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>() {
                        @Override public void onClusterItemInfoWindowClick(ClusterMarker clusterItem) {
                            Toast.makeText(GoogleMapsActivity.this, "Clicked info window: " + clusterItem.getTitle(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            mMap.setOnMarkerClickListener(mClusterManager);

            mMap.setOnInfoWindowClickListener(mClusterManager);
        }
    }

    /*private void setUserPosition() {
        for (UserLocation userLocation : mUserLocations) {
            if (userLocation.getUser().getUserID().equals(FirebaseAuth.getInstance().getUid())) {
                mUserPosition = userLocation;
            }
        }
    }*/

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
        //myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, MyPlacesData.getInstance().getMyPlaces()));
        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
    }

    private void refreshList() {
        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
    }

    private void initTextListener(){

        //mPlacesList = new ArrayList<>();

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
            refreshList();
        }else{
            setList();
            if(searchType.equals("All")) {
                for (MyPlace myPlace : lista) {
                    if (myPlace.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        mPlacesList.add(myPlace);
                        updateSearchList();
                    }
                }
            } else {
                for (MyPlace myPlace : lista) {
                    if (myPlace.getType().equals(searchType) && myPlace.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        mPlacesList.add(myPlace);
                        updateSearchList();
                    }
                }
            }
        }
    }

    private void updateSearchList(){

        //refreshList();
        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
        myPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mSearchText.setText(mPlacesList.get(position).getName());
                geoLocate(mPlacesList.get(position));
            }
        });
    }

    private void geoLocate(MyPlace myPlace){
        LatLng placeLoc = new LatLng(Double.parseDouble(myPlace.getLatitude()), Double.parseDouble(myPlace.getLongitude()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLoc,mMap.getMaxZoomLevel() - 5));
        mPlacesList.clear();
        refreshList();
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void radiusSearchResult(){
        Toast.makeText(GoogleMapsActivity.this, "Upao u funkciju! Radijus je: " + searchRadius, Toast.LENGTH_SHORT).show();
        Location userLocation = mMap.getMyLocation();
        double lat1 = userLocation.getLatitude();
        double lon1 = userLocation.getLongitude();
        for (MyPlace myPlace : lista) {
            if(distance(lat1, lon1, Double.parseDouble(myPlace.getLatitude()), Double.parseDouble(myPlace.getLongitude())) < 10) {
                mPlacesList.add(myPlace);
                Toast.makeText(GoogleMapsActivity.this, "Prvo mesto: : " +  mPlacesList.get(0).getName(), Toast.LENGTH_SHORT).show();
                setList();
            }
        }
    }

    public static Double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        // earth radius is in mile
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(Math.toRadians(lat_a))
                * Math.cos(Math.toRadians(lat_b)) * Math.sin(lngDiff / 2)
                * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;
        double kmConvertion = 1.6093;
        return new Double(distance * kmConvertion).doubleValue();
        //return String.format("%.2f", new Float(distance * kmConvertion).floatValue()) + " km";
        // return String.format("%.2f", distance)+" m";
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
