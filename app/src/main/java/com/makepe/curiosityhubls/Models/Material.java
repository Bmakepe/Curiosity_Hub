package com.makepe.curiosityhubls.Models;

public class Material {

    String document, grade, name, subject, type, level, material_ID, year;

    public Material() {
    }

    public Material(String document, String grade, String name, String subject, String type, String level, String material_ID, String year) {
        this.document = document;
        this.grade = grade;
        this.name = name;
        this.subject = subject;
        this.type = type;
        this.level = level;
        this.material_ID = material_ID;
        this.year = year;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMaterial_ID() {
        return material_ID;
    }

    public void setMaterial_ID(String material_ID) {
        this.material_ID = material_ID;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
