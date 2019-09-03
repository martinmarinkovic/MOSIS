package com.martinmarinkovic.partytime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlaceProfile extends AppCompatActivity {

    private String placeID, userID, placeUserID, friendPlaceID;
    private float rating, ratingsSum, placeRating, oldRating;
    private DatabaseReference myRef, mUserDatabase, mPlaceDatabase;
    private FirebaseUser mCurrentUser;
    private int numOfRatings;
    private List<Rating> ratingsList;
    private Rating myRating;
    private Boolean firtsTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        int position = -1;
        int activity = 0;

        placeID = null;
        rating = 0;
        firtsTime = true;
        myRating = null;

        try {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();
            position = positionBundle.getInt("position");
            activity = positionBundle.getInt("activity");
            friendPlaceID = positionBundle.getString("placeID");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        if (position >= 0) {

            MyPlace place = null;
            if (activity == 1)
                place = MyPlacesData.getInstance().getPlace(position);
            else if (activity == 2)
                place = AllPlacesData.getInstance().getPlace(position);
            else
                place = MyPlacesData.getInstance().findPlace(friendPlaceID);

            placeID = place.getKey();
            placeUserID = place.getUserId();

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(placeUserID).child("my-places").child(placeID);
            mPlaceDatabase = FirebaseDatabase.getInstance().getReference().child("my-places").child(placeID);
            mUserDatabase.keepSynced(true);
            mPlaceDatabase.keepSynced(true);

            userID = mCurrentUser.getUid();
            numOfRatings = place.getNumOfRatings();
            ratingsSum = place.getRatingsSum();
            placeRating = place.getRating();

            getRating();

            TextView twName = (TextView) findViewById(R.id.my_place_name);
            twName.setText(place.getName());
            TextView twDesc = (TextView) findViewById(R.id.my_place_desc);
            twDesc.setText(place.getDescription());
            final ImageView displayImage = (ImageView) findViewById(R.id.my_place_image);
            final String image = place.getImage().toString();

            if (!image.equals("default"))
                Picasso.get().load(image).placeholder(R.drawable.default_place_profile).into(displayImage);
            else
                Picasso.get().load(R.drawable.default_place_profile).into(displayImage);
        }

        Button showOnMap = (Button) findViewById(R.id.map);
        final int finalPosition = position;
        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaceProfile.this, GoogleMapsActivity.class);
                i.putExtra("state", GoogleMapsActivity.CENTER_PLACE_ON_MAP);
                MyPlace place = MyPlacesData.getInstance().getPlace(finalPosition);
                i.putExtra("lat", place.getLatitude());
                i.putExtra("lon", place.getLongitude());
                startActivity(i);
            }
        });

        Button review = (Button) findViewById(R.id.review);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (placeID != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("placeID", placeID);
                    bundle.putString("placeUserID", placeUserID);
                    Intent i = new Intent(PlaceProfile.this, CommentsView.class);
                    i.putExtras(bundle);
                    startActivity(i);
                } else
                    Toast.makeText(PlaceProfile.this, "NIJE NASO MESTO!!! PLACE ID = NULL", Toast.LENGTH_SHORT).show();
            }
        });

        final Button ratings = (Button) findViewById(R.id.ratings);
        ratings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rating >= 0) {
                    Intent i = new Intent(PlaceProfile.this, RatingBarActivity.class);
                    i.putExtra("rating", rating);
                    startActivityForResult(i, 1);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                float result = data.getFloatExtra("userRatingInt", 0);

                if(firtsTime) {
                    numOfRatings++;
                    ratingsSum = ratingsSum + result;
                    placeRating = ratingsSum / numOfRatings;
                    myRating = new Rating(result, userID);
                }else{
                    ratingsSum = ratingsSum - oldRating + result;
                    placeRating = ratingsSum / numOfRatings;
                    myRating = new Rating(result, userID);
                }

                addRating();
                getRating();
            }
            if (resultCode == RESULT_CANCELED) {}
        }
    }

    public void addRating(){

        mPlaceDatabase.child("ratings")
                .child(userID)
                .setValue(myRating);

        mPlaceDatabase.child("numOfRatings")
                .setValue(numOfRatings);

        mPlaceDatabase.child("ratingsSum")
                .setValue(ratingsSum);

        mPlaceDatabase.child("rating")
                .setValue(placeRating);

        mUserDatabase.child("ratings")
                .child(userID)
                .setValue(myRating);

        mUserDatabase.child("numOfRatings")
                .setValue(numOfRatings);

        mUserDatabase.child("ratingsSum")
                .setValue(ratingsSum);

        mUserDatabase.child("rating")
                .setValue(placeRating);
    }

    public void getRating() {

        Query query = myRef
                .child("my-places")
                .child(placeID)
                .child("ratings")
                .orderByKey()
                .equalTo(userID);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                myRating = dataSnapshot.getValue(Rating.class);
                rating = myRating.getRating();
                oldRating = rating;
                firtsTime = false;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            //finish();
            Intent intent = new Intent(PlaceProfile.this, MyPlacesList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PlaceProfile.this, MyPlacesList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}