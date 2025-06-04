package com.example.eduquest;

import com.google.firebase.Timestamp;
import java.util.Map;

public class Post {
    private String postId;
    private String userId;
    private String userName;
    private String userProfileUrl;
    private String content;
    private String imageUrl;
    private Timestamp timestamp;
    private Map<String, String> reactions;

    public Post() {}

    public Post(String postId, String userName, String userProfileUrl, String content, String imageUrl) {
        this.postId = postId;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        this.userProfileUrl = userProfileUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, String> reactions) {
        this.reactions = reactions;
    }
}
