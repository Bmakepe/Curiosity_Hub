package com.makepe.curiosityhubls.Models;

public class Schools {

    String area, description, imgProfile, location, school, school_ID;

    public Schools() {
    }

    public Schools(String area, String description, String imgProfile, String location, String school, String school_ID) {
        this.area = area;
        this.description = description;
        this.imgProfile = imgProfile;
        this.location = location;
        this.school = school;
        this.school_ID = school_ID;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgProfile() {
        return imgProfile;
    }

    public void setImgProfile(String imgProfile) {
        this.imgProfile = imgProfile;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchool_ID() {
        return school_ID;
    }

    public void setSchool_ID(String school_ID) {
        this.school_ID = school_ID;
    }
}
