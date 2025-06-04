package com.example.eduquest;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String userProfileUrl;
    private String content;
    private Timestamp timestamp;

    public Comment() {}

    public Comment(String commentId, String userId, String userName, String userProfileUrl, String content, Timestamp timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;
        this.content = content;
        this.timestamp = timestamp;
    }


    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserProfileUrl() { return userProfileUrl; }
    public void setUserProfileUrl(String userProfileUrl) { this.userProfileUrl = userProfileUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
