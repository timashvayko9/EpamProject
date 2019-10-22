package com.epam.course.filesclient.model;

public interface NoteRepository {

    Iterable<Note> findAll();

    Note findByPath(String path);

    Note save(Note note);

    void deleteNoteByPath(String path);
}
