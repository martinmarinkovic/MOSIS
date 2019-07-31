package com.martinmarinkovic.partytime;

import android.app.Activity;
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
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class RatingTableAdapter extends ArrayAdapter<MyPlace> {

    private Activity context;
    private List<MyPlace> myPlacesLists;
    private String image;

    public RatingTableAdapter(Activity context, @NonNull List<MyPlace> myPlacesLists) {
        super(context, R.layout.my_places_row, myPlacesLists);
        this.context = context;
        this.myPlacesLists = myPlacesLists;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listView = inflater.inflate(R.layout.my_places_row, null, true);

        TextView place_name = (TextView) listView.findViewById(R.id.place_name);
        TextView place_rating = (TextView) listView.findViewById(R.id.place_rating);
        final ImageView place_profile_image = (ImageView) listView.findViewById(R.id.place_profile_image);

        final MyPlace myPlace = myPlacesLists.get(position);
        place_name.setText(myPlace.getName());
        place_rating.setText(""+myPlace.getRating());
        image = myPlace.getImage();

        try { //nije dobro!!! Koci ocitavanje!!!
            if (!image.equals("default")) {
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(place_profile_image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(place_profile_image);
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return listView;
    }
}