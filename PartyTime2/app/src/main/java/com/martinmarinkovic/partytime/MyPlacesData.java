package com.martinmarinkovic.partytime;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class MyPlacesData {

    private ArrayList<MyPlace> myPlaces;
    private HashMap<String, Integer> myPlacesKeyIndexMapping;
    private DatabaseReference database;
    private FirebaseUser mCurrentUser;
    public String current_uid;
    private static final String FIREBASE_CHILD = "my-places";

    private MyPlacesData() {
        myPlaces = new ArrayList<MyPlace>();
        myPlacesKeyIndexMapping = new HashMap<String, Integer>();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = mCurrentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference();
        database.child(FIREBASE_CHILD).keepSynced(true);
        setParentEventListener(updatedEventListener);
        database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
        database.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (updatedEventListener != null)
                updatedEventListener.onListUpdated();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myPlaceKey = dataSnapshot.getKey();
            if (!myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                MyPlace myPlace = dataSnapshot.getValue(MyPlace.class);
                myPlace.key = myPlaceKey;
                myPlaces.add(myPlace);
                myPlacesKeyIndexMapping.put(myPlaceKey, myPlaces.size()-1);

                sort();

                if (updatedEventListener != null)
                    updatedEventListener.onListUpdated();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myPlaceKey = dataSnapshot.getKey();
            MyPlace myPlace = dataSnapshot.getValue(MyPlace.class);
            myPlace.key = myPlaceKey;
            if (myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                int index = myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.set(index, myPlace);
            } else {
                myPlaces.add(myPlace);
                myPlacesKeyIndexMapping.put(myPlaceKey, myPlaces.size()-1);
            }

            sort();

            if (updatedEventListener != null)
                updatedEventListener.onListUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String myPlaceKey = dataSnapshot.getKey();
            if (myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                int index = myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.remove(index);
                //recreateKeyIndexMapping();

                sort();

                if (updatedEventListener != null)
                    updatedEventListener.onListUpdated();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    private static class SingletonHolder {
        public static final MyPlacesData instance = new MyPlacesData();
    }

    public static MyPlacesData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<MyPlace> getMyPlaces() {
        return myPlaces;
    }

    public void addNewPlace(MyPlace place, String key) {
        //String key = database.push().getKey();
        myPlaces.add(place);
        myPlacesKeyIndexMapping.put(key, myPlaces.size() - 1);
        place.key = key;
        place.userId = current_uid;
        database.child(FIREBASE_CHILD).child(key).setValue(place);
    }

    public MyPlace getPlace(int index) {
        return myPlaces.get(index);
    }

    public void deletePlace(String placeID) {
        for (int f=0;f<myPlaces.size();f++){
            if (myPlaces.get(f).key.equals(placeID))
            {
                MyPlace myPlace = myPlaces.get(f);
                database.child(FIREBASE_CHILD).child(myPlaces.get(f).key).removeValue();
                myPlaces.remove(f);
                recreateKeyIndexMapping();
            }
        }
    }

    public MyPlace findPlace(String placeID){
        for (int f=0;f<myPlaces.size();f++){
            if (myPlaces.get(f).key.equals(placeID))
                return  myPlaces.get(f);
        }
        return null;
    }

    public void updatePlace(int index, String name, String desc, String lon, String lat) {
        MyPlace myPlace = myPlaces.get(index);
        myPlace.name = name;
        myPlace.description = desc;
        myPlace.latitude = lat;
        myPlace.longitude = lon;
        database.child(FIREBASE_CHILD).child(myPlace.key).setValue(myPlace);
    }

    public void updatePlace(String placeID, String name, String desc) {
        for (int f=0;f<myPlaces.size();f++){
            if (myPlaces.get(f).key.equals(placeID))
            {
                MyPlace myPlace = myPlaces.get(f);
                myPlace.name = name;
                myPlace.description = desc;
                database.child(FIREBASE_CHILD).child(myPlace.key).setValue(myPlace);
            }
        }
    }

    public void recreateKeyIndexMapping() {
        myPlacesKeyIndexMapping.clear();
        for (int i = 0; i < myPlaces.size(); i++)
            myPlacesKeyIndexMapping.put(myPlaces.get(i).key, i);
    }


    public void sort() {

        for (int i = 0; i < myPlaces.size(); i++)
            for (int j = 0; j < myPlaces.size(); j++)
                if (myPlaces.get(i).rating > myPlaces.get(j).rating) {
                    MyPlace myPlace = myPlaces.get(i);
                    myPlaces.set(i, myPlaces.get(j));
                    myPlaces.set(j, myPlace);
                }

        recreateKeyIndexMapping();
    }

    ListUpdatedEventListener updatedEventListener;
    public void setParentEventListener(ListUpdatedEventListener listener) {
        updatedEventListener = listener;
    }

    public interface ListUpdatedEventListener {
        void onListUpdated();
    }
}

