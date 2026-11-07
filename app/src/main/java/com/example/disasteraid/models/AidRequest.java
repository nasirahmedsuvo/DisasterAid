
package com.example.disasteraid.models;

public class AidRequest {

    private String requestId;
    private String userId;
    private String title;
    private String category;
    private String description;
    private String location;
    private String quantity;
    private String status;
    private long createdAt;
    private long updatedAt;

    // Required empty constructor for Firebase
    public AidRequest() {
    }

    public AidRequest(String requestId, String userId,
                      String title, String category,
                      String description, String location,
                      String quantity, String status,
                      long createdAt, long updatedAt) {

        this.requestId = requestId;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.location = location;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
