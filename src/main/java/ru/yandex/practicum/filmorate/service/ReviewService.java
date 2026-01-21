package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class ReviewService {
    public final ReviewStorage reviewStorage;
    public final UserStorage userStorage;
    public final FilmStorage filmStorage;
    @Autowired
    private UserService userService;

    public ReviewService(@Autowired @Qualifier("reviewDbStorage")ReviewStorage reviewStorage,
                         @Autowired @Qualifier("userDbStorage") UserStorage userStorage,
                         @Autowired @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review add(Review review) {
        validateFilmExists(review.getFilmId()); // проверка на существования фильма
        validateUserExists(review.getUserId()); // проверка на существования пользователя

        review.setUseful(0);
        reviewStorage.add(review);
        userService.addEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.ADD);

        return review;
    }

    public Review update(Review review) {
        validateFilmExists(review.getFilmId());
        validateUserExists(review.getUserId());

        Long reviewId  = review.getReviewId();
        Review existing = reviewStorage.getById(reviewId);
        if (!existing.getUserId().equals(review.getUserId())) {
            throw new ValidationException("Пользователь не должен меняться");
        }
        if (!existing.getFilmId().equals(review.getFilmId())) {
            throw new ValidationException("Фильм не должен меняться");
        }

        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        existing.setUseful(review.getUseful());
        userService.addEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.UPDATE);

        return reviewStorage.update(existing);
    }

    public void delete(Long id) {
        Review review = getById(id);
        userService.addEvent(review.getUserId(), id, EventType.REVIEW, Operation.REMOVE);
        reviewStorage.delete(id);
    }

    public Review getById(Long id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getAll() {
        return reviewStorage.getAll();
    }

    public List<Review> getAllReviewByFilmId(Long filmId, int count) {
        if (filmId != null) {
            validateFilmExists(filmId);
        }
        return reviewStorage.getAllReviewByFilmId(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        getById(reviewId); // проверка существования отзыва
        validateUserExists(userId); // проверка существования пользователя
        userService.addEvent(userId, reviewId, EventType.LIKE, Operation.ADD);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDisLike(Long reviewId, Long userId) {
        getById(reviewId);
        validateUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        getById(reviewId);
        validateUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDisLike(Long reviewId, Long userId) {
        getById(reviewId);
        validateUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
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
