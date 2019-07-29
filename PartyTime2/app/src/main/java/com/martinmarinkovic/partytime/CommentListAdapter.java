package com.martinmarinkovic.partytime;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private Activity context;
    private List<Comment> commentsList;
    private String image;

    public CommentListAdapter(Activity context, @NonNull List<Comment> commentsList) {
        super(context, R.layout.layout_comment, commentsList);
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listView = inflater.inflate(R.layout.layout_comment, null, true);

        TextView comment = (TextView)listView.findViewById(R.id.comment);
        final TextView username = (TextView)listView.findViewById(R.id.comment_username);
        TextView timestamp = (TextView)listView.findViewById(R.id.comment_time_posted);
        final CircleImageView profileImage = (CircleImageView) listView.findViewById(R.id.comment_profile_image);

        final Comment c = commentsList.get(position);
        comment.setText(c.getComment());

        //set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Users")
                .orderByChild("userID")
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    username.setText( singleSnapshot.getValue(User.class).getFirstname());
                    image = singleSnapshot.getValue(User.class).getImage().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            timestamp.setText(timestampDifference + " days ago");
        }else{
            timestamp.setText("today");
        }

        try { //nije dobro!!! Koci ocitavanje!!!
            if (!image.equals("default")) {
                //placeHolder stavljamo zbog problema kada treba malo vremen da ocita sliku
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profileImage);
                    }
                });
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return listView;
    }

    private String getTimestampDifference(Comment comment){
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            difference = "0";
        }
        return difference;
    }

}
