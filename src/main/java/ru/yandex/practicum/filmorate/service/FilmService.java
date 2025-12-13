package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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
    }

    public void deleteLike(long id, long userId) {
        filmStorage.getById(id)
                .getLikes()
                .remove(userStorage.getById(userId).getId());
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .collect(Collectors.toMap(x -> x, x -> x.getLikes().size()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
