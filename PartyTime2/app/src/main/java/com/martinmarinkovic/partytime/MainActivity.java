package com.martinmarinkovic.partytime;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import java.net.CacheRequest;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private CardView cv1, cv2, cv3, cv4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
        getSupportActionBar().setTitle("Party Time");
        auth = FirebaseAuth.getInstance();

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
                Intent i = new Intent(MainActivity.this, UserProfile.class);
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
                Intent i = new Intent(MainActivity.this, AllPlacesList.class);
                startActivity(i);
            }
        });
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
        } else if (id == R.id.about) {
            Intent i = new Intent(this, About.class);
            startActivity(i);
        } else if (id == R.id.sign_out) {
            auth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
