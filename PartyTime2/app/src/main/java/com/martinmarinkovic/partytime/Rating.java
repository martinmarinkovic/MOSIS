package com.martinmarinkovic.partytime;

public class Rating {

    public float rating;
    public String user_id;

    public Rating() {
    }

    public Rating(float rating, String user_id) {
        this.rating = rating;
        this.user_id = user_id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
