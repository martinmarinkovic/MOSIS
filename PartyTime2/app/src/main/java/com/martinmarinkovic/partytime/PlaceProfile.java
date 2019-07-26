package com.martinmarinkovic.partytime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class PlaceProfile extends AppCompatActivity {

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
        //String placeID;
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

            TextView twName = (TextView) findViewById(R.id.my_place_name);
            twName.setText(place.getName());
            TextView twDesc = (TextView) findViewById(R.id.my_place_desc);
            twDesc.setText(place.getDescription());
            final ImageView displayImage = (ImageView) findViewById(R.id.my_place_image);
            final String image = place.getImage().toString();

            if (!image.equals("default")) {
                //placeHolder stavljamo zbog problema kada treba malo vremen da ocita sliku
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
        final Button startQuiz = (Button) findViewById(R.id.start_quiz);
        startQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}