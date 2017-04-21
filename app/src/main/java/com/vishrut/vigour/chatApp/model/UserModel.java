package com.vishrut.vigour.chatApp.model;

import com.google.firebase.database.Exclude;


public class UserModel {

    private String id;
    private String name;
    private String photo_profile;

    public UserModel() {
    }

    public UserModel(String name, String photo_profile, String id) {
        this.name = name;
        if (photo_profile == null || photo_profile.isEmpty()) {
            this.photo_profile = "https://cdn1.iconfinder.com/data/icons/ninja-things-1/1772/ninja-simple-512.png";
        } else
            this.photo_profile = photo_profile;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_profile() {
        return photo_profile;
    }

    public void setPhoto_profile(String photo_profile) {
        this.photo_profile = photo_profile;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
