package com.example.androidchat.login;

import com.google.firebase.database.Exclude;

public class User {
    public String name;
    public String email;
    @Exclude
    public String password;
    public String id;
    public String avatar;
    public boolean online;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
