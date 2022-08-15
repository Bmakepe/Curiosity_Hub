package com.makepe.curiosityhubls.Models;

public class PostModel {
    String Caption, PostImage, pId, uid, PostTime;

    public PostModel() {
    }

    public PostModel(String caption, String postImage, String pId, String uid, String postTime) {
        Caption = caption;
        PostImage = postImage;
        this.pId = pId;
        this.uid = uid;
        PostTime = postTime;
    }

    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getPostImage() {
        return PostImage;
    }

    public void setPostImage(String postImage) {
        PostImage = postImage;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostTime() {
        return PostTime;
    }

    public void setPostTime(String postTime) {
        PostTime = postTime;
    }
}
