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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class UserData {

    private ArrayList<UserLocation> usersLocationList;
    private ArrayList<User> usersList;
    private HashMap<String, Integer> myPlacesKeyIndexMapping;
    private DatabaseReference database;
    private FirebaseUser mCurrentUser;
    public String current_uid;
    private static final String FIREBASE_CHILD = "Users";

    private UserData() {
        usersList = new ArrayList<User>();
        usersLocationList = new ArrayList<UserLocation>();
        myPlacesKeyIndexMapping = new HashMap<String, Integer>();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = mCurrentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference();
        database.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String userKey = dataSnapshot.getKey();
            if (!myPlacesKeyIndexMapping.containsKey(userKey)) {
                User user = dataSnapshot.getValue(User.class);
                user.userID = userKey;
                usersList.add(user);
                getUserLocations(user);
                myPlacesKeyIndexMapping.put(userKey, usersList.size()-1);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String userKey = dataSnapshot.getKey();
            User user = dataSnapshot.getValue(User.class);
            user.userID = userKey;
            if (myPlacesKeyIndexMapping.containsKey(userKey)) {
                int index = myPlacesKeyIndexMapping.get(userKey);
                usersList.set(index, user);
                getUserLocations(user);
            } else {
                usersList.add(user);
                getUserLocations(user);
                myPlacesKeyIndexMapping.put(userKey, usersList.size()-1);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String userKey = dataSnapshot.getKey();
            if (myPlacesKeyIndexMapping.containsKey(userKey)) {
                int index = myPlacesKeyIndexMapping.get(userKey);
                usersList.remove(index);
                recreateKeyIndexMapping();
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
        public static final UserData instance = new UserData();
    }

    public static UserData getInstance() {
        return UserData.SingletonHolder.instance;
    }

    public ArrayList<User> getUsersList() {
        return usersList;
    }

    public void addNewUser(User user) {
        String key = database.push().getKey();
        usersList.add(user);
        myPlacesKeyIndexMapping.put(key, usersList.size() - 1);
        user.key = key;
        database.child(FIREBASE_CHILD).child(key).setValue(user);
    }

    public User getUser(int index) {
        return usersList.get(index);
    }

    public void recreateKeyIndexMapping() {
        myPlacesKeyIndexMapping.clear();
        for (int i = 0; i < usersList.size(); i++)
            myPlacesKeyIndexMapping.put(usersList.get(i).key, i);
    }

    public void getUserLocations(User user){
        database.child("User Locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds != null){
                        UserLocation userLocation = ds.getValue(UserLocation.class);
                        usersLocationList.add(userLocation);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public ArrayList<UserLocation> getUserLocationsList(){
        return usersLocationList;
    }
}
