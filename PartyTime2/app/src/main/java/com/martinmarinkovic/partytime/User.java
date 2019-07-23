package com.martinmarinkovic.partytime;

public class User {
    public String firstname;
    public String lastname;
    public String image;
    public int score;
    public String userID;
    public String status;
    public String thumb_image;

    public User() {
    }

    public User(String firstname, String lastname, String image, int score, String userID, String status, String thumb_image) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.image = image;
        this.score = score;
        this.userID = userID;
        this.status = status;
        this.thumb_image = thumb_image;
    }

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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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
}