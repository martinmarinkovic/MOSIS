package com.martinmarinkovic.partytime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllPlacesList extends AppCompatActivity {

    public String placeID, userID, activity;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_places_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("my-places");

        final ListView myPlacesList = (ListView)findViewById(R.id.my_places_list);
        myPlacesList.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, AllPlacesData.getInstance().getMyPlaces()));
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
                MyPlace place = AllPlacesData.getInstance().getPlace(info.position);
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
        MyPlace myplace = AllPlacesData.getInstance().getPlace(info.position);
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
            AllPlacesData.getInstance().deletePlace(info.position);
            MyPlacesData.getInstance().deletePlace(placeID);//??????????????
            setList();
        } else if (item.getItemId() == 4) {
            i = new Intent(this, GoogleMapsActivity.class);
            i.putExtra("state", GoogleMapsActivity.CENTER_PLACE_ON_MAP);
            MyPlace place = AllPlacesData.getInstance().getPlace(info.position);
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
        myPlacesList.setAdapter(new ArrayAdapter<MyPlace>(this, android.R.layout.simple_list_item_1, AllPlacesData.getInstance().getMyPlaces()));
    }
}
