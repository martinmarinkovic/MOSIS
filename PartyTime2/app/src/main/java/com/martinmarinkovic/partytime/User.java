package com.martinmarinkovic.partytime;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class User implements Parcelable {
    public String firstname;
    public String lastname;
    public String username;
    public String image;
    public String userID;
    public String status;
    public String thumb_image;

    @Exclude
    public String key;


    public User() {
    }

    public User(String firstname, String lastname, String image, String userID, String status, String thumb_image) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.image = image;
        this.userID = userID;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public User(String firstname, String lastname, String username, String image, String userID, String status, String thumb_image) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.image = image;
        this.userID = userID;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    protected User(Parcel in) {
        firstname = in.readString();
        lastname = in.readString();
        username = in.readString();
        image = in.readString();
        userID = in.readString();
        status = in.readString();
        thumb_image = in.readString();
        key = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(username);
        dest.writeString(image);
        dest.writeString(userID);
        dest.writeString(status);
        dest.writeString(thumb_image);
        dest.writeString(key);
    }
}