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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
    private Spinner spinner;
    private EditText etName,etDesc, latEdit, lonEdit;
    private Button addButton, locationButton, cancelButton;
    private ProgressDialog mProgressDialog;
    public String AB;
    public SecureRandom rnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_place);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Add place");

        addButton = (Button)findViewById(R.id.editmyplace_finished_button);
        cancelButton = (Button)findViewById(R.id.editmyplace_cancel_button);
        locationButton = (Button) findViewById(R.id.editmyplace_location_button);
        etName = (EditText)findViewById(R.id.editmyplace_name_edit);
        etDesc = (EditText)findViewById(R.id.editmyplace_desc_edit);
        latEdit = (EditText) findViewById(R.id.editmyplace_lat_edit);
        lonEdit = (EditText) findViewById(R.id.editmyplace_lon_edit);
        spinner = (Spinner) findViewById(R.id.spinner);

        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);

        AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        rnd = new SecureRandom();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.editmyplace_finished_button: {

                String name = etName.getText().toString();
                String desc = etDesc.getText().toString();
                String lat = latEdit.getText().toString();
                String lon = lonEdit.getText().toString();
                String type = spinner.getSelectedItem().toString();

                if(name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(lat.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter coordinates!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(lon.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter coordinates!", Toast.LENGTH_SHORT).show();
                    return;
                }

                MyPlace place = new MyPlace(name, desc, "default", 0, 0,0);
                place.setLatitude(lat);
                place.setLongitude(lon);
                place.setType(type);
                //String key = database.push().getKey();
                String key = randomString(20);
                MyPlacesData.getInstance().addNewPlace(place, key);
                AllPlacesData.getInstance().addNewPlace(place, key);

                setResult(Activity.RESULT_OK);

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
        } catch (Exception e) {}
    }

    public String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}

