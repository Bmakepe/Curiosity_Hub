package com.makepe.curiosityhubls.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {

    private String sender, receiver, message, audio, timeStamp, msg_type, media;
    private ArrayList<String> mediaList;
    private boolean isSeen;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, String audio, String timeStamp, String msg_type, String media, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.audio = audio;
        this.timeStamp = timeStamp;
        this.msg_type = msg_type;
        this.media = media;
        this.isSeen = isSeen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public ArrayList<String> getMediaList() {
        return mediaList;
    }

    public void setMediaList(ArrayList<String> mediaList) {
        this.mediaList = mediaList;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}

