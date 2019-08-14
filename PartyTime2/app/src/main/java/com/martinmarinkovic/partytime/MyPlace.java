package com.martinmarinkovic.partytime;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class MyPlace {

    public String name;
    public String description;
    public String longitude;
    public String latitude;
    public String image;
    public float rating;
    public int numOfRatings;
    public float ratingsSum;
    public String type;
    //public List<Comment> comments;
    //public List<Rating> ratings;
    public String userId;

    @Exclude
    public String key;

    public MyPlace() {}

    /*public MyPlace(String name, String description, String image, float rating, int numOfRatings, float ratingsSum, String type, List<Comment> comments, List<Rating> ratings) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.rating = rating;
        this.numOfRatings = numOfRatings;
        this.ratingsSum = ratingsSum;
        this.type = type;
        this.comments = comments;
        this.ratings = ratings;
    }*/

    public MyPlace(String name, String description, String image, float rating, int numOfRatings, float ratingsSum, String userId) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.rating = rating;
        this.numOfRatings = numOfRatings;
        this.ratingsSum = ratingsSum;
        this.userId = userId;
    }

    public MyPlace(String name, String description, String image, float rating, int numOfRatings, float ratingsSum) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.rating = rating;
        this.numOfRatings = numOfRatings;
        this.ratingsSum = ratingsSum;
    }

    public MyPlace(String name, String description, String image, float rating) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.rating = rating;
        this.numOfRatings = 0;
        this.ratingsSum = 0;
    }

    public MyPlace(String name, String description, String image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getNumOfRatings() {
        return numOfRatings;
    }

    public void setNumOfRatings(int numOfRatings) {
        this.numOfRatings = numOfRatings;
    }

    public float getRatingsSum() {
        return ratingsSum;
    }

    public void setRatingsSum(float ratingsSum) {
        this.ratingsSum = ratingsSum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /*public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }*/
}
