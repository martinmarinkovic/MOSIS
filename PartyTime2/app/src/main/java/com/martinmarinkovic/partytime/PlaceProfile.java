package com.martinmarinkovic.partytime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class PlaceProfile extends AppCompatActivity {

    private String placeID;
    private float rating;
    private DatabaseReference myRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int position = -1;
        int activity = 0;
        placeID = null;
        rating = -1;
        myRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        try {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();
            position = positionBundle.getInt("position");
            activity = positionBundle.getInt("activity");
            //placeID = positionBundle.getString("placeID");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        if (position >= 0) {

            MyPlace place = null;

            if (activity == 1)
                place = MyPlacesData.getInstance().getPlace(position);
            else
                place = AllPlacesData.getInstance().getPlace(position);

            placeID = place.getKey();
            rating = place.getRating();

            TextView twName = (TextView) findViewById(R.id.my_place_name);
            twName.setText(place.getName());
            TextView twDesc = (TextView) findViewById(R.id.my_place_desc);
            twDesc.setText(place.getDescription());
            final ImageView displayImage = (ImageView) findViewById(R.id.my_place_image);
            final String image = place.getImage().toString();

            if (!image.equals("default")) {
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(displayImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(displayImage);
                    }
                });
            }

        }

        Button startQuiz = (Button) findViewById(R.id.start_quiz);
        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (placeID != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("placeID", placeID);
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
                    Toast.makeText(PlaceProfile.this, rating +"", Toast.LENGTH_SHORT).show();
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
                if(resultCode == RESULT_OK){
                    //String result = data.getStringExtra("userRating");
                    float result = data.getFloatExtra("userRatingInt", 0);
                    Toast.makeText(PlaceProfile.this, result +"", Toast.LENGTH_SHORT).show();

                    String current_uid = mCurrentUser.getUid();

                    myRef.child("my-places")
                            .child(placeID)
                            .child("rating")
                            .setValue(result);

                    myRef.child("Users")
                            .child(current_uid)
                            .child("my-places")
                            .child(placeID)
                            .child("rating")
                            .setValue(result);

                }
                if (resultCode == RESULT_CANCELED) {
                    //Write your code if there's no result
                }
            }
        }
}