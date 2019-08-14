package com.martinmarinkovic.partytime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendPlacesList extends AppCompatActivity {

    private FirebaseUser mCurrentUser;
    private DatabaseReference myRef;
    private ListView mListView;
    public ArrayList<MyPlace> placesList;
    private String userID, current_userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_places_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (ListView) findViewById(R.id.listView);
        placesList = new ArrayList<MyPlace>();
        myRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_userID = mCurrentUser.getUid();

        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            userID = bundle.getString("userID");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        Query query = myRef
                .child("Users")
                .child(userID)
                .child("my-places");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()) {
                    MyPlace place = singleSnapshot.getValue(MyPlace.class);
                    placesList.add(place);
                }

                mListView.setAdapter(new ArrayAdapter<MyPlace>(FriendPlacesList.this, android.R.layout.simple_list_item_1, placesList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String placeID = placesList.get(position).getID();
                Toast.makeText(FriendPlacesList.this, placesList.get(position).getName() +"  :  "+ placeID, Toast.LENGTH_SHORT).show();
                Bundle positionBundle = new Bundle();
                positionBundle.putString("placeID", placeID);
                positionBundle.putInt("activity", 3);
                Intent i = new Intent(FriendPlacesList.this, PlaceProfile.class);
                i.putExtras(positionBundle);
                startActivity(i);
            }
        });
    }
}