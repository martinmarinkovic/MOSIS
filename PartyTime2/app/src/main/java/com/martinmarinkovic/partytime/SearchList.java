package com.martinmarinkovic.partytime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchList extends AppCompatActivity {

    private EditText mSearchText;
    private List<MyPlace> mPlacesList, lista, places, placesInRadius;
    private ListView myPlacesListView;
    private String searchRadius, searchType;
    private ImageView searchBtn;
    private Double radious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

        searchBtn = (ImageView) findViewById(R.id.select_catgory);
        mSearchText = (EditText) findViewById(R.id.input_search);
        myPlacesListView = (ListView)findViewById(R.id.listView);
        lista = MyPlacesData.getInstance().getMyPlaces();
        places = new ArrayList<>();
        placesInRadius = new ArrayList<>();

        UserLocation currentUserLocation = UserData.getInstance().getCurrentUserLocation();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchList.this, SearchByAtributes.class);
                startActivity(intent);
            }
        });

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

        if (!searchType.equals("All"))
            for (MyPlace myPlace : lista) {
                if (myPlace.getType().equals(searchType)) {
                    places.add(myPlace);
                }
            }
        else
            places.addAll(lista);

        if(!searchRadius.equals("None")) {

            if (searchRadius.equals("1 KM"))
                radious = 1.0;
            if (searchRadius.equals("5 KM"))
                radious = 5.0;
            if (searchRadius.equals("10 KM"))
                radious = 10.0;

            double lat1 = currentUserLocation.getGeo_point().getLatitude();
            double lon1 = currentUserLocation.getGeo_point().getLongitude();

            for (MyPlace myPlace : places) {
                if (getDistance(lat1, lon1, Double.parseDouble(myPlace.getLatitude()), Double.parseDouble(myPlace.getLongitude())) < radious) {
                    placesInRadius.add(myPlace);
                }
            }
            places.clear();
            places.addAll(placesInRadius);
        }

        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(SearchList.this, android.R.layout.simple_list_item_1, places));
        myPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MyPlace place = MyPlacesData.getInstance().findPlace(places.get(position).key);
                if (place != null) {
                    Intent i = new Intent(SearchList.this, GoogleMapsActivity.class);
                    i.putExtra("state", GoogleMapsActivity.CENTER_PLACE_ON_MAP);
                    i.putExtra("lat", place.getLatitude());
                    i.putExtra("lon", place.getLongitude());
                    startActivity(i);
                }

                hideSoftKeyboard();
            }
        });

        hideSoftKeyboard();
        initTextListener();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SearchList.this, GoogleMapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
                Toast.makeText(SearchList.this, mSearchText.getText().toString(), Toast.LENGTH_SHORT).show();
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword){

        mPlacesList.clear();
        if(keyword.length() ==0){
            myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, places));
        }else{
            myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
            if(searchType.equals("All")) {
                for (MyPlace myPlace : places) {
                    if (myPlace.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        mPlacesList.add(myPlace);
                        updateSearchList();
                    }
                }
            } else {
                for (MyPlace myPlace : places) {
                    if (myPlace.getType().equals(searchType) && myPlace.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        mPlacesList.add(myPlace);
                        updateSearchList();
                    }
                }
            }
        }
    }

    private void updateSearchList(){

        myPlacesListView.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, mPlacesList));
        myPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MyPlace place = MyPlacesData.getInstance().findPlace(mPlacesList.get(position).key);
                if (place != null) {
                    Toast.makeText(SearchList.this, "DA!!!!!!!!!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(SearchList.this, GoogleMapsActivity.class);
                    i.putExtra("state", GoogleMapsActivity.CENTER_PLACE_ON_MAP);
                    i.putExtra("lat", place.getLatitude());
                    i.putExtra("lon", place.getLongitude());
                    startActivity(i);
                }else
                    Toast.makeText(SearchList.this, "DA!!!!!!!!!", Toast.LENGTH_SHORT).show();
                hideSoftKeyboard();
            }
        });
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static Double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {

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
