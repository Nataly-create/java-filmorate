package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    Film getById(long id);

    List<Film> getAll();

    void delete(Film film);

    void addLike(long id, User user);

    void deleteLike(long id, User user);

    List<Film> getFilmsByDirector(long directorId);

    public Map<Long, Set<Long>> getAllLikes();

    boolean existsById(long id);

    void deleteById(long id);
}
