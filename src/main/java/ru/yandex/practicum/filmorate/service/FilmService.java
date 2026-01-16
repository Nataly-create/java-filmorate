package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class FilmService {
    public final FilmStorage filmStorage;
    public final UserStorage userStorage;
    public final GenreStorage genreStorage;

    FilmService(@Autowired @Qualifier("filmDbStorage") FilmStorage filmStorage,
                @Autowired @Qualifier("userDbStorage") UserStorage userStorage,
                @Autowired @Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
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
    }

    public void deleteLike(long id, long userId) {
        filmStorage.getById(id)
                .getLikes()
                .remove(userStorage.getById(userId).getId());
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        Stream<Film> stream = filmStorage.getAll().stream();
        if (year != 0) {
            stream = stream.filter(f -> f.getReleaseDate().getYear() == year);
        }
        if (genreId != null) {
            stream = stream.filter(f -> f.getGenres().contains(genreStorage.getById(genreId)));
        }

        stream = stream.sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
        if (count != 0) {
            stream = stream.limit(count);
        }
        return stream.toList();
    }
}
