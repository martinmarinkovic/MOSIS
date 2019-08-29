package com.martinmarinkovic.partytime;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class RatingBarActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextView tv;
    private Button ratingBtn;
    private float rating;
    public String userRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_bar);

        ratingBar = findViewById(R.id.rate);
        tv = findViewById(R.id.tv);
        ratingBtn = findViewById(R.id.user_rating);

        try {
            Intent intent = getIntent();
            rating = intent.getFloatExtra("rating", 0);
        }catch (Exception e){}

        ratingBar.setRating(rating);
        tv.setText(String.valueOf(rating));

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                tv.setText(String.valueOf(ratingBar.getRating()));
            }
        });

        ratingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRating = tv.getText().toString();
                float userRatingInt = Float.parseFloat(userRating);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("userRatingInt", userRatingInt);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }
}