package com.makepe.curiosityhubls.Models;

public class User {

    String name, gender, dateOfBirth, profileImg, userId, bio, grade, phoneNumber, school, role, district;

    public User() {
    }

    public User(String name, String gender, String dateOfBirth, String profileImg, String userId, String bio, String grade, String phoneNumber, String school, String role, String district) {
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.profileImg = profileImg;
        this.userId = userId;
        this.bio = bio;
        this.grade = grade;
        this.phoneNumber = phoneNumber;
        this.school = school;
        this.role = role;
        this.district = district;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
