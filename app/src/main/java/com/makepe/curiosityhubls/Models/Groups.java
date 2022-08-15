package com.makepe.curiosityhubls.Models;

public class Groups {
    String groupID, groupName, groupIcon, role, groupAdmin, groupPrivacy, timestamp;

    public Groups() {
    }

    public Groups(String groupID, String groupName, String groupIcon, String role, String groupAdmin, String groupPrivacy, String timestamp) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupIcon = groupIcon;
        this.role = role;
        this.groupAdmin = groupAdmin;
        this.groupPrivacy = groupPrivacy;
        this.timestamp = timestamp;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String getGroupPrivacy() {
        return groupPrivacy;
    }

    public void setGroupPrivacy(String groupPrivacy) {
        this.groupPrivacy = groupPrivacy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
