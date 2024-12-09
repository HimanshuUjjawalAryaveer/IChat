package com.example.ichat.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Users implements Parcelable {
    private String userName;
    private String email;
    private String status;

    protected Users(Parcel in) {
        userName = in.readString();
        email = in.readString();
        status = in.readString();
        password = in.readString();
        image = in.readString();
        about = in.readString();
        education = in.readString();
        address = in.readString();
        id = in.readString();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Users(String userName, String email, String password, String about, String education, String address, String id, String status) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.about = about;
        this.education = education;
        this.address = address;
        this.id = id;
        this.status = status;
    }

    private String password;
    private String image;
    private String about;
    private String education;

    public Users(String userName, String email, String password, String image, String about, String education, String address, String id, String status) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.image = image;
        this.about = about;
        this.education = education;
        this.address = address;
        this.id = id;
        this.status = status;
    }

    private String address;

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEducation() {
        return education;
    }

    public Users(String userName) {
        this.userName = userName;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Users(String userName, String email, String password, String id) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.id = id;
    }
    public Users(String userName, String email, String password, String image, String id) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.image = image;
        this.id = id;
    }
    public Users() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeString(status);
        dest.writeString(password);
        dest.writeString(image);
        dest.writeString(about);
        dest.writeString(education);
        dest.writeString(address);
        dest.writeString(id);
    }
}
