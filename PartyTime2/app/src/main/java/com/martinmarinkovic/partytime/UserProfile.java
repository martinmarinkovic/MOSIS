package com.martinmarinkovic.partytime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class UserProfile extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private CircleImageView mDisplayImage;
    private TextView mName, mStatus, mUsername;
    private Button mImageBtn, mPlacesList;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;
    private String userID, profile_user_id, activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
        getSupportActionBar().setTitle("Profile");

        try {
            Intent listIntent = getIntent();
            Bundle positionBundle = listIntent.getExtras();
            userID = positionBundle.getString("userID");
            activity = positionBundle.getString("activity");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.profile_name);
        mStatus = (TextView) findViewById(R.id.profile_status);
        mImageBtn = (Button) findViewById(R.id.settings_image_btn);
        mUsername = (TextView) findViewById(R.id.profile_username);
        mPlacesList = (Button) findViewById(R.id.show_my_placesBtn);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (activity.equals("FriendsList")) {
            profile_user_id = userID;
            mImageBtn.setVisibility(View.INVISIBLE);
        }
        else if(activity.equals("Main")) {
            profile_user_id = mCurrentUser.getUid();
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(profile_user_id);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("firstname").getValue().toString() + " " +
                        dataSnapshot.child("lastname").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                //String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                String username = dataSnapshot.child("username").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                mUsername.setText(username);

                if (!image.equals("default"))
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                else
                    Picasso.get().load(R.drawable.default_avatar).into(mDisplayImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        mPlacesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("userID", profile_user_id);
                Intent i = new Intent(UserProfile.this, FriendPlacesList.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(UserProfile.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                final Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                String current_user_id = mCurrentUser.getUid();
                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(100)
                            .setMaxHeight(100)
                            .setQuality(100)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map update_hashMap = new HashMap();
                                update_hashMap.put("image", uri.toString());
                                //update_hashMap.put("thumb_image", thumb_downloadUrl);

                                mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(UserProfile.this, "Success Uploading.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (activity.equals("Main")) {
            getMenuInflater().inflate(R.menu.menu_user_profile, menu);
            return true;
        } else {
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }
        if (id == R.id.settings) {
            String status_value = mStatus.getText().toString();
            String username_value = mUsername.getText().toString();
            Intent settings_intent = new Intent(UserProfile.this, EditUserProfile.class);
            settings_intent.putExtra("status_value", status_value);
            settings_intent.putExtra("username_value", username_value);
            startActivity(settings_intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
