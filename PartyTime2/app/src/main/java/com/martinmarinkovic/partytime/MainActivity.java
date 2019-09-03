package com.martinmarinkovic.partytime;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.net.CacheRequest;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private CardView cv1, cv2, cv3, cv4;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    private String current_uid;
    private static final String TAG = "MainActivity";
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private DatabaseReference myRef;
    private Switch sw;
    private ArrayList<MyPlace> list1, list2;
    private ArrayList<User> list3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
        getSupportActionBar().setTitle("Party Time");

        auth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = mCurrentUser.getUid();
        myRef = FirebaseDatabase.getInstance().getReference();

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();
        list3 = new ArrayList<>();
        list1 = MyPlacesData.getInstance().getMyPlaces();
        list2 = AllPlacesData.getInstance().getMyPlaces();
        list3 = UserData.getInstance().getUsersList();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cv1 = (CardView) findViewById(R.id.cv1);
        cv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GoogleMapsActivity.class);
                i.putExtra("state", GoogleMapsActivity.SHOW_MAP);
                startActivity(i);
            }
        });

        cv2 = (CardView) findViewById(R.id.cv2);
        cv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("friendID", "");
                bundle.putString("activity", "Main");
                Intent i = new Intent(MainActivity.this, UserProfile.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        cv3 = (CardView) findViewById(R.id.cv3);
        cv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyPlacesList.class);
                startActivity(i);
            }
        });

        cv4 = (CardView) findViewById(R.id.cv4);
        cv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, FriendsList.class);
                startActivity(i);
            }
        });


        sw = (Switch) findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startLocationService();
                } else {
                    Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                    stopService(new Intent(serviceIntent));
                }
            }
        });
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MainActivity.this.startForegroundService(serviceIntent); // Da bi ostao da radi, da se ne bi zaustavio
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.martinmarinkovic.partytime.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void getUserDetails(){
        if(mUserLocation == null) {
            mUserLocation = new UserLocation();

            Query query = myRef
                    .child("Users")
                    .orderByKey()
                    .equalTo(current_uid);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        User user = dataSnapshot1.getValue(User.class);
                        if (user != null) {
                            mUserLocation.setUser(user);
                            ((UserClient) (getApplicationContext())).setUser(user);
                            getLastKnownLocation();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        } else{
            //getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    saveUserLocation();
                    //startLocationService();
                }
            }
        });

    }

    private void saveUserLocation(){

        myRef.child("User Locations")
                .child(current_uid)
                .setValue(mUserLocation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: \ninserted user location into database." +
                                    "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                                    "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                        }
                    }
                });
    }


    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available == ConnectionResult.SUCCESS){
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_new_friend) {
            Intent i = new Intent(this, AddNewFriend.class);
            startActivity(i);
        } else if (id == R.id.my_places_list) {
            Bundle bundle = new Bundle();
            bundle.putString("userID", current_uid);
            Intent i = new Intent(MainActivity.this, AllPlacesList.class);
            i.putExtras(bundle);
            startActivity(i);
        } else if (id == R.id.about) {
            Intent i = new Intent(this, About.class);
            startActivity(i);
        } else if (id == R.id.sign_out) {
            auth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getUserDetails();
            }
            else{
                getLocationPermission();
            }
        }
    }
}
