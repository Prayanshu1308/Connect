package com.hncc.connect;

public class feed {

    String profileImageUrl, profileName, postDateAndTime, postImageUrl, postMessage, postLikes, postUid, postPushId;

    public feed(String profileImageUrl, String profileName, String postDateAndTime, String postImageUrl, String postMessage, String postLikes, String postUid, String postPushId) {
        this.profileImageUrl = profileImageUrl;
        this.profileName = profileName;
        this.postDateAndTime = postDateAndTime;
        this.postImageUrl = postImageUrl;
        this.postMessage = postMessage;
        this.postLikes = postLikes;
        this.postUid = postUid;
        this.postPushId = postPushId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPostDateAndTime() {
        return postDateAndTime;
    }

    public void setPostDateAndTime(String postDateAndTime) {
        this.postDateAndTime = postDateAndTime;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getPostMessage() {
        return postMessage;
    }

    public void setPostMessage(String postMessage) {
        this.postMessage = postMessage;
    }

    public String getPostLikes() {
        return postLikes;
    }

    public void setPostLikes(String postLikes) {
        this.postLikes = postLikes;
    }

    public String getPostUid() {
        return postUid;
    }

    public void setPostUid(String postUid) {
        this.postUid = postUid;
    }

    public String getPostPushId() {
        return postPushId;
    }

    public void setPostPushId(String postPushId) {
        this.postPushId = postPushId;
    }
}
