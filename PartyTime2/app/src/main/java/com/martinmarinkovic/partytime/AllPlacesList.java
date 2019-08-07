package com.martinmarinkovic.partytime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class AllPlacesList extends AppCompatActivity {

    public String placeID, userID, activity;
    public AllPlacesData allPlacesData;
    public List<MyPlace> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_places_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            Intent listIntent = getIntent();
            Bundle userBundle = listIntent.getExtras();
            userID = userBundle.getString("userID");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        allPlacesData = new AllPlacesData(userID);
        //list = allPlacesData.getInstance().getMyPlaces();
        populateListView(allPlacesData.getInstance().getMyPlaces());
    }

    @Override
    protected void onStart() {
        super.onStart();

        //list.clear();

        try {
            Intent listIntent = getIntent();
            Bundle userBundle = listIntent.getExtras();
            userID = userBundle.getString("userID");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        allPlacesData = new AllPlacesData(userID);
        populateListView(allPlacesData.getInstance().getMyPlaces());
    }

    public void populateListView(List<MyPlace> list){
        final ListView myPlacesList = (ListView)findViewById(R.id.my_places_list);
        myPlacesList.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, list));
        myPlacesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle positionBundle = new Bundle();
                positionBundle.putInt("position", position);
                positionBundle.putInt("activity", 2);
                Intent i = new Intent(AllPlacesList.this, PlaceProfile.class);
                i.putExtras(positionBundle);
                startActivity(i);
            }
        });

        myPlacesList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                MyPlace place = allPlacesData.getInstance().getPlace(info.position);
                menu.setHeaderTitle(place.getName());
                menu.add(0, 1, 1, "View place");
                menu.add(0, 2, 2, "Edit place");
                menu.add(0, 3, 3, "Remove place");
                menu.add(0, 4, 4, "Show on map");
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MyPlace myplace = allPlacesData.getInstance().getPlace(info.position);
        placeID = myplace.getID();
        Bundle positionBundle = new Bundle();
        positionBundle.putInt("position", info.position);
        positionBundle.putInt("activity", 2);
        Intent i = null;
        if (item.getItemId() == 1) {
            i = new Intent(this, PlaceProfile.class);
            i.putExtras(positionBundle);
            startActivity(i);
        } else if (item.getItemId() == 2) {
            i = new Intent(this, EditMyPlace.class);
            i.putExtras(positionBundle);
            startActivityForResult(i, 1);
        } else if (item.getItemId() == 3) {
            allPlacesData.getInstance().deletePlace(info.position);
            MyPlacesData.getInstance().deletePlace(placeID);//??????????????
            setList();
        } else if (item.getItemId() == 4) {
            i = new Intent(this, GoogleMapsActivity.class);
            i.putExtra("state", GoogleMapsActivity.CENTER_PLACE_ON_MAP);
            MyPlace place = allPlacesData.getInstance().getPlace(info.position);
            i.putExtra("lat", place.getLatitude());
            i.putExtra("lon", place.getLongitude());
            startActivityForResult(i, 2);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            setList();
        }
    }

    private void setList() {
        ListView myPlacesList = (ListView) findViewById(R.id.my_places_list);
        myPlacesList.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, allPlacesData.getInstance().getMyPlaces()));
    }
}
