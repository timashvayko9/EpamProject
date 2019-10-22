package com.epam.course.filesclient.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class JdbcNoteRepository implements NoteRepository {

    private JdbcTemplate jdbc;

    @Autowired
    public JdbcNoteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Iterable<Note> findAll() {
        return jdbc.query("select path, note from Note", this::mapRowToNote);
    }

    @Override
    public Note findByPath(String path) {
        return jdbc.queryForObject(
                "select path, note from Note where path=?",
                this::mapRowToNote, path);
    }

    @Override
    public Note save(Note note) {
        jdbc.update(
                "insert into Note ( path, note) values (?, ?)",
                note.getPath(),
                note.getNote());
        return note;
    }

    private Note mapRowToNote(ResultSet rs, int rowNum) throws SQLException {
        return new Note(
                rs.getString("path"),
                rs.getString("note"));
    }

    @Override
    public void deleteNoteByPath(String path) {
        jdbc.update("delete from Note where path = ?", path);
    }
}
