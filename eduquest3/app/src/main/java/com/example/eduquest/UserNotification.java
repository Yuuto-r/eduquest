package com.example.eduquest;

import com.google.firebase.Timestamp;

public class UserNotification {
    private String type;
    private String senderName;
    private String message;
    private String postId;
    private Timestamp timestamp;

    public UserNotification() {
    }

    public UserNotification(String type, String senderName, String message, String postId, Timestamp timestamp) {
        this.type = type;
        this.senderName = senderName;
        this.message = message;
        this.postId = postId;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
