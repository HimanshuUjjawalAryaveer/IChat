package com.example.ichat.Model;

public class User {
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String address;
    private String about;
    private String education;
    private String password;
    private String email;
    private String username;
    private String userID;

    public User() {

    }
    public User(String username, String email, String password, String userID, String imageUrl, String address, String about, String education, String status) {
        this.address = address;
        this.about = about;
        this.education = education;
        this.password = password;
        this.email = email;
        this.username = username;
        this.userID = userID;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    private String imageUrl;
    private String status;

}
