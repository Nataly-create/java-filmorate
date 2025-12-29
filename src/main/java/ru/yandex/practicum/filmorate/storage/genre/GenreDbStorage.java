package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final Logger log = LoggerFactory.getLogger(GenreController.class);
    protected final JdbcTemplate jdbc;
    private final GenreRowMapper rowMapper;
    private static final String GET_ALL_QUERY = "SELECT * FROM genre";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";
    private static final String GET_GENRE_BY_ID_QUERY =
            "SELECT f.genre_id genre_id, g.name name FROM films_genre f LEFT JOIN genre g " +
                    "on f.genre_id = g.genre_id WHERE f.film_id = ? ORDER BY f.genre_id";

    @Override
    public List<Genre> getAll() {
        return jdbc.query(GET_ALL_QUERY, rowMapper);
    }

    @Override
    public Genre getById(int id) {
        try {
            return jdbc.queryForObject(GET_BY_ID_QUERY, rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Genre mit id {} not found", id);
            throw new NotFoundException(id, "Genre");
        }
    }

    @Override
    public List<Genre> getGenresById(int id) {
        return jdbc.query(GET_GENRE_BY_ID_QUERY, rowMapper, id);
    }
}
