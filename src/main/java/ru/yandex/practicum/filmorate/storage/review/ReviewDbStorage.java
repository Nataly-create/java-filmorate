package ru.yandex.practicum.filmorate.storage.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.ReviewController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@Qualifier("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewDbStorage(ReviewRowMapper mapper, JdbcTemplate jdbc,
                           @Autowired @Qualifier("userDbStorage") UserStorage userStorage,
                           @Autowired @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.mapper = mapper;
        this.jdbc = jdbc;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Review add(Review review) {
        validateFilmExists(review.getFilmId());
        validateUserExists(review.getUserId());
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        review.setReviewId(generatedId);

        log.info("Review with id {} added", generatedId);
        return review;
    }

    @Override
    public Review update(Review review) {
        long id = review.getReviewId();
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ?, useful = ? " +
                "WHERE review_id = ?";

        int rowsAffected = jdbc.update(sql, review.getContent(), review.getIsPositive(), review.getUserId(),
                review.getFilmId(), review.getUseful(), id);

        if (rowsAffected == 0) {
            throw new NotFoundException(id, "Review");
        }

        log.info("Review with id {} updated", id);
        return getById(id);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        int rowsAffected = jdbc.update(sql, id);

        if (rowsAffected == 0) {
            throw new NotFoundException(id, "Review");
        }

        log.info("Review with id {} deleted", id);
    }

    @Override
    public Review getById(Long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbc.queryForObject(sql, mapper, id);
        } catch (Exception e) {
            throw new NotFoundException(id, "Review");
        }
    }

    @Override
    public List<Review> getAll() {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC";
        List<Review> reviews = jdbc.query(sql, mapper);
        return reviews;
    }

    @Override
    public List<Review> getAllReviewByFilmId(Long filmId, int count) {
        if (filmId == null) {
            String sqlAll = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            return jdbc.query(sqlAll, mapper, count);
        } else {
            String sqlByFilm = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            return jdbc.query(sqlByFilm, mapper, filmId, count);
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        Review review = getById(reviewId);
        validateUserExists(userId); // проверка существования пользователя

        String checkSql = "SELECT COUNT(*) FROM review_reactions WHERE review_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count > 0) { // Пользователь уже голосовал - ничего не делаем
            return;
        }

        String insertSql = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, true)";
        jdbc.update(insertSql, reviewId, userId);

        String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbc.update(updateSql, reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        Review review = getById(reviewId);
        validateUserExists(userId); // проверка существования пользователя

        String checkSql = "SELECT COUNT(*) FROM review_reactions WHERE review_id = ? AND user_id = ?";
        try {
            Boolean currentReaction = jdbc.queryForObject(checkSql, Boolean.class, reviewId, userId);

            if (!currentReaction) { // Уже стоит дизлайк
                return;
            } else {
                String updateSql = "UPDATE review_reactions SET is_like = false WHERE review_id = ? AND user_id = ?";
                jdbc.update(updateSql, reviewId, userId);

                String updateUsefulSql = "UPDATE reviews SET useful = useful - 2 WHERE review_id = ?";
                jdbc.update(updateUsefulSql, reviewId);
                return;
            }
        } catch (Exception e) {
            // Нет реакции
        }

        String insertSql = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, false)";
        jdbc.update(insertSql, reviewId, userId);

        String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbc.update(updateSql, reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String deleteSql = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ? AND is_like = true";
        int rows = jdbc.update(deleteSql, reviewId, userId);

        if (rows > 0) {
            String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
            jdbc.update(updateSql, reviewId);
        }
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String deleteSql = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ? AND is_like = false";
        int rows = jdbc.update(deleteSql, reviewId, userId);

        if (rows > 0) {
            String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
            jdbc.update(updateSql, reviewId);
        }
    }

    public void validateFilmExists(Long filmId) {
        try {
            filmStorage.getById(filmId);
        } catch (NotFoundException e) {
            throw new NotFoundException(filmId, "Film");
        }
    }

    public void validateUserExists(Long userId) {
        try {
            userStorage.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException(userId, "User");
        }
    }
}
