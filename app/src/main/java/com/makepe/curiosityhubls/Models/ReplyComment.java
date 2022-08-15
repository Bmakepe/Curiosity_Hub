package com.makepe.curiosityhubls.Models;

public class ReplyComment {
    String replyID, commentID, userID, comment, timeStamp;

    public ReplyComment() {
    }

    public ReplyComment(String replyID, String commentID, String userID, String comment, String timeStamp) {
        this.replyID = replyID;
        this.commentID = commentID;
        this.userID = userID;
        this.comment = comment;
        this.timeStamp = timeStamp;
    }

    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
