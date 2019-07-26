package com.martinmarinkovic.partytime;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddNewPlace.class);
                startActivity(intent);
            }
        });

        imageView = (ImageView) findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GoogleMapsActivity.class);
                i.putExtra("state", GoogleMapsActivity.SHOW_MAP);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent i = new Intent(this, UserProfile.class);
            startActivity(i);
        }else if (id == R.id.table) {
            Intent i = new Intent(this, MyPlacesList.class);
            startActivity(i);
        }else if (id == R.id.my_places_list) {
            Intent i = new Intent(this, MyPlacesList.class);
            startActivity(i);
        }else if (id == R.id.about) {
            Intent i = new Intent(this, AllPlacesList.class);
            startActivity(i);
        }
        else if (id == R.id.sign_out) {
            auth.signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
