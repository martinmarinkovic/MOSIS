package com.martinmarinkovic.partytime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

public class AddMyPlacePhoto extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUserDatabase, mPlaceDatabase;
    private FirebaseUser mCurrentUser;
    public String placeID, longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_my_place_photo);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        try {
            Intent listIntent = getIntent();
            Bundle bundle = listIntent.getExtras();

            if (bundle != null){
                placeID = bundle.getString("placeID");
                longitude = bundle.getString("lon");
                latitude = bundle.getString("lat");
                mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                String current_uid = mCurrentUser.getUid();
                mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).child("my-places").child(placeID);
                mPlaceDatabase = FirebaseDatabase.getInstance().getReference().child("my-places").child(placeID);
                mUserDatabase.keepSynced(true);
                mPlaceDatabase.keepSynced(true);
            }
            else {
                placeID = null;
            }
        } catch (Exception e) {
        }

        Button imageButton = (Button) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(2, 1)
                    .setMinCropWindowSize(1000, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(AddMyPlacePhoto.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                final Uri resultUri = result.getUri();
                final StorageReference filepath = mImageStorage.child("places_profile_images").child(placeID + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            final String download_url = resultUri.toString();
                            Map update_hashMap = new HashMap();
                            update_hashMap.put("image", download_url);
                            mPlaceDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        Toast.makeText(AddMyPlacePhoto.this, "Success Uploading.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        Toast.makeText(AddMyPlacePhoto.this, "Success Uploading.", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });

                            Intent i = new Intent(AddMyPlacePhoto.this, GoogleMapsActivity.class);
                            i.putExtra("state", GoogleMapsActivity.CENTER_PLACE_ON_MAP);
                            i.putExtra("lat", latitude);
                            i.putExtra("lon", longitude);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivityForResult(i, 2);

                        }else{
                            Toast.makeText(AddMyPlacePhoto.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
