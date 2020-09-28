package com.hncc.connect;

public class feedPost {

    private String postMessageImage, userName, postMessageText, dateAndTime, postPushID, likes, uid;

    public feedPost() {}

    public String getPostPushID() {
        return postPushID;
    }

    public void setPostPushID(String postPushID) {
        this.postPushID = postPushID;
    }

    public String getPostMessageImage() {
        return postMessageImage;
    }

    public void setPostMessageImage(String postMessageImage) {
        this.postMessageImage = postMessageImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostMessageText() {
        return postMessageText;
    }

    public void setPostMessageText(String postMessageText) {
        this.postMessageText = postMessageText;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
