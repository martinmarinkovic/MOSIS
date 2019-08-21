package com.martinmarinkovic.partytime;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.Date;

public class UserLocation {

    private User user;
    private GeoPoint geo_point;
    private @ServerTimestamp
    Date timestamp;

    public UserLocation() {
    }

    public UserLocation(User user, GeoPoint geo_point, Date timestamp) {
        this.user = user;
        this.geo_point = geo_point;
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
