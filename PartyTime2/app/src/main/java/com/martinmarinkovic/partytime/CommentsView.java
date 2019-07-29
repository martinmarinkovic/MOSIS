package com.martinmarinkovic.partytime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class CommentsView extends AppCompatActivity {

    private static final String TAG = "CommentsView";
    String placeID;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;
    protected ArrayList<Comment> mComments;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments_view);

        mBackArrow = (ImageView) findViewById(R.id.backArrow);
        mCheckMark = (ImageView) findViewById(R.id.ivPostComment);
        mComment = (EditText) findViewById(R.id.comment);
        mListView = (ListView) findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        mComments = new ArrayList<Comment>();


        try {
            Intent listIntent = getIntent();
            Bundle bundle = listIntent.getExtras();
            placeID = bundle.getString("placeID");
        } catch (Exception e) {
        }

        getComment();

        /*if(!mComments.isEmpty()) {
            CommentListAdapter adapter = new CommentListAdapter(mContext,
                    R.layout.layout_comment, mComments);
            mListView.setAdapter(adapter);
        }*/

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyboard();
                }else{
                    Toast.makeText(CommentsView.this, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void closeKeyboard(){
        View view = getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        Toast.makeText(CommentsView.this, mComment.getText().toString(), Toast.LENGTH_SHORT).show();
        String commentID = myRef.push().getKey();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(userID);

        myRef.child("my-places")
                .child(placeID)//Prosledjujemo kad klikenmo na ikonu za komentare
                .child("comments")
                .child(commentID)
                .setValue(comment);

        myRef.child("Users")
                .child(userID)
                .child("my-places")
                .child(placeID)//Prosledjujemo kad klikenmo na ikonu za komentare
                .child("comments")
                .child(commentID)
                .setValue(comment);
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));
        return sdf.format(new Date());
    }

    public void getComment() {
        /*
        myRef.child("my-places")
                .child(placeID)
                .child("comments")
                .addChildEventListener(new ChildEventListener() { //da bi komentari uvek bili azurirani
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: child added.");

                        Query query = myRef
                                .child("my-places")
                                .orderByChild("key")
                                .equalTo(placeID);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                    for (DataSnapshot dSnapshot : singleSnapshot
                                            .child("comments").getChildren()){
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }
                                    //place.setComments(mComments);
                                    //setupWidgets();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled.");
                            }
                        });
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) { }
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
        */

        Query query = myRef
                .child("my-places")
                .child(placeID)
                .child("comments");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                mComments.add(comment);

                CommentListAdapter adapter = new CommentListAdapter(CommentsView.this, mComments);
                mListView.setAdapter(adapter);
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

        /*
        Query query = myRef
                .child("my-places")
                .child(placeID)
                .child("comments");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Comment comment = singleSnapshot.getValue(Comment.class);
                    Toast.makeText(CommentsView.this, comment.toString(), Toast.LENGTH_SHORT).show();
                    mComments.add(comment);
                }

                Query query = FirebaseDatabase.getInstance().getReference()
                        .child("places")
                        .child(placeID);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        CommentListAdapter adapter = new CommentListAdapter(CommentsView.this, mComments);
                        mListView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });*/
    }
}
