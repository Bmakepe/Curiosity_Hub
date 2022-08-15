package com.makepe.curiosityhubls.Models;

public class Comment {

    String cID, Comment, Timestamp, Name, uid;

    public Comment() {
    }

    public Comment(String cID, String comment, String timestamp, String name, String uid) {
        this.cID = cID;
        Comment = comment;
        Timestamp = timestamp;
        Name = name;
        this.uid = uid;
    }

    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
