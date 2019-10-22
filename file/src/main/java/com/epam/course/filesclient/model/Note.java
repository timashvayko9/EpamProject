package com.epam.course.filesclient.model;

public class Note {

    public Note(String path, String note) {
        this.path = path;
        this.note = note;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    private String path;

    private String note;
}
