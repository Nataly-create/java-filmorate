package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage {
    private final JdbcTemplate jdbc;
    private static final String GET_FILMS_ID_BY_DIRECTOR_QUERY = """
        SELECT DISTINCT film_id
        FROM film_directors
        WHERE director_id IN (
            SELECT id
            FROM directors
            WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))
        );
    """;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Director> getAll() {
        String sql = "SELECT * FROM directors ORDER BY id";
        return jdbc.query(sql, this::mapDirector);
    }

    public Optional<Director> getById(long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        try {
            Director director = jdbc.queryForObject(sql, this::mapDirector, id);
            return Optional.ofNullable(director);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        if (rows == 0) {
            throw new RuntimeException("Не удалось создать режиссёра");
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Не удалось получить ID после создания режиссёра");
        }

        director.setId(key.longValue());
        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int updated = jdbc.update(sql, director.getName(), director.getId());

        if (updated == 0) {
            throw new IllegalArgumentException("Режиссёр с id " + director.getId() + " не найден.");
        }

        return director;
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM film_directors WHERE director_id = ?", id);
        int deleted = jdbc.update("DELETE FROM directors WHERE id = ?", id);
        if (deleted == 0) {
            throw new IllegalArgumentException("Режиссёр с id " + id + " не найден.");
        }
    }

    // === Внутренний маппер ===
    private Director mapDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }

    public List<Director> getDirectorsByFilmId(int filmId) {
        String sql = "SELECT d.id, d.name FROM directors d " +
                "JOIN film_directors fd ON d.id = fd.director_id " +
                "WHERE fd.film_id = ?";
        return jdbc.query(sql, this::mapDirector, filmId);
    }

    public Collection<Long> getFilmsIdByDirector(String query) {
        return jdbc.queryForList(GET_FILMS_ID_BY_DIRECTOR_QUERY, Long.class, query);
    }
}
