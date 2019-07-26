package com.martinmarinkovic.partytime;

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

public class AllPlacesData {

    private ArrayList<MyPlace> myPlaces;
    private HashMap<String, Integer> myPlacesKeyIndexMapping;
    private DatabaseReference database;
    private FirebaseUser mCurrentUser;

    private AllPlacesData() {
        myPlaces = new ArrayList<MyPlace>();
        myPlacesKeyIndexMapping = new HashMap<String, Integer>();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).child("my-places");
        database.addChildEventListener(childEventListener);
        database.addListenerForSingleValueEvent(parentEventListener);

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
            if (updatedEventListener != null)
                updatedEventListener.onListUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String myPlaceKey = dataSnapshot.getKey();
            if (myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                int index = myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.remove(index);
                recreateKeyIndexMapping();

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
        public static final AllPlacesData instance = new AllPlacesData();
    }

    public static AllPlacesData getInstance() {
        return AllPlacesData.SingletonHolder.instance;
    }

    public ArrayList<MyPlace> getMyPlaces() {
        return myPlaces;
    }

    public MyPlace findPlace(String placeID){
        for (int f=0;f<myPlaces.size();f++){
            if (myPlaces.get(f).key.equals(placeID))
                return  myPlaces.get(f);
        }
        return null;
    }

    public void addNewPlace(MyPlace place, String key) {
        //String key = database.push().getKey();
        myPlaces.add(place);
        myPlacesKeyIndexMapping.put(key, myPlaces.size() - 1);
        database.child(key).setValue(place);
        place.key = key;
    }

    public MyPlace getPlace(int index) {
        return myPlaces.get(index);
    }

    public void deletePlace(int index) {
        database.child(myPlaces.get(index).key).removeValue();
        myPlaces.remove(index);
        recreateKeyIndexMapping();
    }

    public void updatePlace(int index, String name, String desc, String lon, String lat) {
        MyPlace myPlace = myPlaces.get(index);
        myPlace.name = name;
        myPlace.description = desc;
        myPlace.latitude = lat;
        myPlace.longitude = lon;
        database.child(myPlace.key).setValue(myPlace);
    }

    public void updatePlace(int index, String name, String desc) {
        MyPlace myPlace = myPlaces.get(index);
        myPlace.name = name;
        myPlace.description = desc;
        database.child(myPlace.key).setValue(myPlace);
    }

    public void recreateKeyIndexMapping() {
        myPlacesKeyIndexMapping.clear();
        for (int i = 0; i < myPlaces.size(); i++)
            myPlacesKeyIndexMapping.put(myPlaces.get(i).key, i);
    }

    AllPlacesData.ListUpdatedEventListener updatedEventListener;
    public void setParentEventListener(AllPlacesData.ListUpdatedEventListener listener) {
        updatedEventListener = listener;
    }

    public interface ListUpdatedEventListener {
        void onListUpdated();
    }
}
