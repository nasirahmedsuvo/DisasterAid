package com.example.disasteraid.models;

public class Volunteer {

    private String volunteerId;
    private String userId;
    private String name;
    private String phone;
    private String skills;
    private String availability;
    private String location;

    // Required empty constructor for Firebase
    public Volunteer() {
    }

    public Volunteer(
            String volunteerId,
            String userId,
            String name,
            String phone,
            String skills,
            String availability,
            String location) {

        this.volunteerId = volunteerId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.skills = skills;
        this.availability = availability;
        this.location = location;
    }

    public String getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
