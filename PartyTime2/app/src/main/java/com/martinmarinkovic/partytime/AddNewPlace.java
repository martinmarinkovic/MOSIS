package com.martinmarinkovic.partytime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddNewPlace extends AppCompatActivity implements View.OnClickListener {

    int position = -1;
    private ProgressDialog mProgressDialog;
    String AB;
    SecureRandom rnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_place);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Add place");

        final Button buttonFinished = (Button)findViewById(R.id.editmyplace_finished_button);
        Button cancelButton = (Button)findViewById(R.id.editmyplace_cancel_button);
        EditText nameEditText = (EditText)findViewById(R.id.editmyplace_name_edit);

        AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        rnd = new SecureRandom();

        buttonFinished.setEnabled(false);
        buttonFinished.setText("Add");
        buttonFinished.setOnClickListener(this);
        buttonFinished.setEnabled(false);
        cancelButton.setOnClickListener(this);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonFinished.setEnabled(s.length() > 0);
            }
        });

        Button locationButton = (Button) findViewById(R.id.editmyplace_location_button);
        locationButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.editmyplace_finished_button: {
                EditText etName = (EditText)findViewById(R.id.editmyplace_name_edit);
                String name = etName.getText().toString();
                EditText etDesc = (EditText)findViewById(R.id.editmyplace_desc_edit);
                String desc = etDesc.getText().toString();
                EditText latEdit = (EditText) findViewById(R.id.editmyplace_lat_edit);
                String lat = latEdit.getText().toString();
                EditText lonEdit = (EditText) findViewById(R.id.editmyplace_lon_edit);
                String lon = lonEdit.getText().toString();
                ImageView img = (ImageView) findViewById(R.id.my_place_image);

                MyPlace place = new MyPlace(name, desc, "default", 0);
                place.setLatitude(lat);
                place.setLongitude(lon);
                //String key = database.push().getKey();
                String key = randomString(20);
                MyPlacesData.getInstance().addNewPlace(place, key);//da dodam neki random string generator
                AllPlacesData.getInstance().addNewPlace(place, key);

                setResult(Activity.RESULT_OK);
                //finish();

                //Float flat = Float.parseFloat(lat);
                //Float flon = Float.parseFloat(lon);
                Bundle bundle = new Bundle();
                bundle.putString("placeID", key);
                bundle.putString("lat", lat);
                bundle.putString("lon", lon);
                Intent i = new Intent(AddNewPlace.this, AddMyPlacePhoto.class);
                i.putExtras(bundle);
                startActivity(i);

                break;
            }

            case R.id.editmyplace_cancel_button: {
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            }
            case R.id.editmyplace_location_button: {
                Intent i = new Intent(this, GoogleMapsActivity.class);
                i.putExtra("state", GoogleMapsActivity.SELECT_COORDINATES);
                startActivityForResult(i, 2);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == Activity.RESULT_OK) {
                String lon = data.getExtras().getString("lon");
                EditText lonTex = (EditText) findViewById(R.id.editmyplace_lon_edit);
                lonTex.setText(lon);
                String lat = data.getExtras().getString("lat");
                EditText latText = (EditText) findViewById(R.id.editmyplace_lat_edit);
                latText.setText(lat);
            }
        } catch (Exception e) {
            // TODO
        }
    }

    String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}

