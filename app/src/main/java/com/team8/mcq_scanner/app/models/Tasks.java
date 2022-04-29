package com.team8.mcq_scanner.app.models;

public class Tasks {
    private String userId;
    private String imgId;
    private String createdAt;
    private String imgUri;
    private String imgName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    @Override
    public String toString() {
        return "Tasks{" +
                "userId='" + userId + '\'' +
                ", imgId='" + imgId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", imgUri='" + imgUri + '\'' +
                ", imgName='" + imgName + '\'' +
                '}';
    }
}
