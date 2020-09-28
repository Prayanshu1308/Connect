package com.hncc.connect;

public class person {

    private String name;
    private String uid;
    private int followers;

    public person(String name, int followers, String uid) {
        this.name = name;
        this.followers = followers;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
