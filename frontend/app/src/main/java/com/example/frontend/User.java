package com.example.frontend;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String userId;
    private String userToken;
    private String email;
    private String code;
    private String name;
    private String city;

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

    public User(String userId, String  userToken, String email, String code) {

        this.userId = userId;
        this.userToken = userToken;
        this.email = email;
        this.code = code;
        this.city = "Vancouver";//set the default city to Vancouver
    }

    public String getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.userId = id;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String token) {
        this.userToken = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    //parcelling part
    protected User(Parcel in) {
        userId = in.readString();
        userToken = in.readString();
        email = in.readString();
        code = in.readString();
        city = in.readString();
        name = in.readString();

    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(userToken);
        parcel.writeString(email);
        parcel.writeString(code);
        parcel.writeString(city);
        parcel.writeString(name);
    }
}
