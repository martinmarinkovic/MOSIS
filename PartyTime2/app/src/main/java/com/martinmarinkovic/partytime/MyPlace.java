package com.martinmarinkovic.partytime;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyPlace {

    public String name;
    public String description;
    public String longitude;
    public String latitude;
    public String image;

    public MyPlace(String name, String description, String image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    @Exclude
    public String key;

    public MyPlace() {}

    public MyPlace(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public MyPlace (String name) {
        this(name, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongitude() { return this.longitude; }

    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getLatitude() { return this.latitude; }

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getID() {
        return key;
    }

    public void setID(String ID) {
        this.key = ID;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
