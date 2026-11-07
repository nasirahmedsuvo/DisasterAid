
package com.example.disasteraid.models;

public class Resource {

    private String resourceId;
    private String userId;
    private String resourceName;
    private String category;
    private String quantity;
    private String location;
    private long createdAt;

    // Required empty constructor for Firebase
    public Resource() {
    }

    public Resource(
            String resourceId,
            String userId,
            String resourceName,
            String category,
            String quantity,
            String location,
            long createdAt) {

        this.resourceId = resourceId;
        this.userId = userId;
        this.resourceName = resourceName;
        this.category = category;
        this.quantity = quantity;
        this.location = location;
        this.createdAt = createdAt;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
