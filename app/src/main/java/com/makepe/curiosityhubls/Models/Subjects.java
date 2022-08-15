package com.makepe.curiosityhubls.Models;

public class Subjects {
    String subjectName, document, grade, name, type;

    public Subjects() {
    }

    public Subjects(String subjectName, String document, String grade, String name, String type) {
        this.subjectName = subjectName;
        this.document = document;
        this.grade = grade;
        this.name = name;
        this.type = type;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
