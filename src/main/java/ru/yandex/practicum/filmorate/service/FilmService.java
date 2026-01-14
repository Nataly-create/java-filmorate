package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class FilmService {
    public final FilmStorage filmStorage;
    public final UserStorage userStorage;

    FilmService(@Autowired @Qualifier("filmDbStorage") FilmStorage filmStorage, @Autowired @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film add(Film film) {
        film.validate();
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        film.validate();
        return filmStorage.update(film);
    }

    public void delete(Film film) {
        filmStorage.delete(film);
    }

    public Film getById(long id) {
        return filmStorage.getById(id);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(long id, long userId) {
        filmStorage.addLike(id, userStorage.getById(userId));
        userStorage.addEvent(userId, id, EventType.LIKE, Operation.ADD);
    }

    public void deleteLike(long id, long userId) {
        filmStorage.getById(id)
                .getLikes()
                .remove(userStorage.getById(userId).getId());
        userStorage.addEvent(userId, id, EventType.LIKE, Operation.REMOVE);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
