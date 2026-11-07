package com.example.disasteraid.models;

public class User {

    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private long createdAt;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String uid, String fullName, String email,
                String phone, String role, long createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}