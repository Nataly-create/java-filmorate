package ru.yandex.practicum.filmorate.storage.film;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.sql.ResultSet;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    protected final JdbcTemplate jdbc;
    protected final FilmRowMapper mapper;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private static final String GET_ALL_QUERY = "SELECT * FROM films";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String ADD_QUERY = "INSERT INTO films(name, description, mpa_id, release_date, duration)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, mpa_id = ?, release_date = ?, duration = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(user_id, film_id) VALUES (?, ?)";
    private static final String ADD_FILMS_GENRE_QUERY = "INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE user_id = ? and film_id = ?";
    private static final String DELETE_FILMS_GENRE_QUERY = "DELETE FROM films_genre WHERE film_id = ?";
    private static final String ADD_FILM_DIRECTOR_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String GET_ALL_LIKES_QUERY = "SELECT user_id, film_id FROM likes";

    public FilmDbStorage(FilmRowMapper mapper, JdbcTemplate jdbc,
                         @Autowired MpaStorage mpaStorage,
                         @Autowired GenreStorage genreStorage,
                         @Autowired GenreRowMapper genreRowMapper) {
        this.mapper = mapper;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.jdbc = jdbc;
    }

    public Film add(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(ADD_QUERY, Statement.RETURN_GENERATED_KEYS);

            int mpaId = film.getMpa().getId();
            mpaStorage.getById(mpaId);

            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getMpa().getId());
            ps.setDate(4, Date.valueOf(film.getReleaseDate()));
            ps.setLong(5, film.getDuration());
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            film.setId(Long.valueOf(id));

            int[] ids = film.getGenres().stream().mapToInt(Genre::getId).toArray();
            film.setGenres(new LinkedHashSet<>(genreStorage.getManyById(ids)));
            updateFilmsGenres(film);

            // Сохраняем режиссёров
            updateFilmDirectors(film);

            film.validate();
            log.info("Film {} added", film);
        }
        return film;
    }

    private void updateFilmDirectors(Film film) {
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());
        List<Object[]> batchArgs = new ArrayList<>();
        for (Director director : film.getDirectors()) {
            batchArgs.add(new Object[]{film.getId(), director.getId()});
        }
        jdbc.batchUpdate(ADD_FILM_DIRECTOR_QUERY, batchArgs);
    }

    private void updateFilmsGenres(Film film) {
        jdbc.update(DELETE_FILMS_GENRE_QUERY, film.getId());
        List<Object[]> params = new ArrayList<>();
        for (Genre g : film.getGenres()) {
            params.add(new Object[]{film.getId(), g.getId()});
        }
        jdbc.batchUpdate(ADD_FILMS_GENRE_QUERY, params);
    }

    public Film update(Film film) {
        long id = film.getId();
        if (jdbc.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getMpa().getId(),
                film.getReleaseDate(),
                film.getDuration(),
                id) > 0) {

            int[] ids = film.getGenres().stream().mapToInt(Genre::getId).toArray();
            film.setGenres(new LinkedHashSet<>(genreStorage.getManyById(ids)));

            updateFilmsGenres(film);
            updateFilmDirectors(film);
            film.validate();
            log.info("Film {} updated", film);
            return film;
        } else {
            log.warn("Film mit id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
    }

    public Film getById(long id) {
        try {
            return jdbc.queryForObject(GET_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Film mit id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
    }

    public List<Film> getAll() {
        return jdbc.query(GET_ALL_QUERY, mapper);
    }

    public void delete(Film film) {
        jdbc.update(DELETE_BY_ID_QUERY, film.getId());
        log.info("User {} deleted", film);
    }

    public void addLike(long id, User user) {
        getById(id);
        jdbc.update(ADD_LIKE_QUERY,
                user.getId(),
                id);
        log.info("Like for id {} added", id);

    }

    public void deleteLike(long id, User user) {
        getById(id);
        jdbc.update(DELETE_LIKE_QUERY,
                user.getId(),
                id);
        log.info("Like for id {} deleted", id);
    }

    public List<Film> getFilmsByDirector(long directorId) {
        String sql = "SELECT f.* FROM films f " +
                "JOIN film_directors fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY f.film_id";
        List<Film> films = jdbc.query(sql, mapper, directorId);
        // Режиссёры и другие данные уже загружены через FilmRowMapper
        return films;
    }

    public Map<Long, Set<Long>> getAllLikes() {
        return jdbc.query(GET_ALL_LIKES_QUERY, (ResultSet rs) -> {
            Map<Long, Set<Long>> result = new HashMap<>();
            while (rs.next()) {
                long userId = rs.getLong("user_id");
                long filmId = rs.getLong("film_id");

                result.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
            }
            return result;
        });
    }
}
