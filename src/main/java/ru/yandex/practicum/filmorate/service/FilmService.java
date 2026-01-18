package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final DirectorService directorService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreStorage genreStorage,
                       DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.directorService = directorService;
    }

    public Film add(Film film) {
        log.debug("Создание фильма: {}", film.getName());
        film.validate();
        Film saved = filmStorage.add(film);
        log.info("Фильм добавлен: '{}', ID={}", saved.getName(), saved.getId());
        return saved;
    }

    public Film update(Film film) {
        log.debug("Обновление фильма: {}", film.getId());
        film.validate();
        Film updated = filmStorage.update(film);
        log.info("Фильм обновлён: '{}', ID={}", updated.getName(), updated.getId());
        return updated;
    }

    public void delete(Film film) {
        log.debug("Удаление фильма: {}", film.getId());
        filmStorage.delete(film);
        log.info("Фильм удалён: '{}'", film.getName());
    }

    public Film getById(long id) {
        log.debug("Получение фильма по ID: {}", id);
        return filmStorage.getById(id);
    }

    public List<Film> getAll() {
        log.debug("Получение всех фильмов");
        return filmStorage.getAll();
    }

    public void addLike(long filmId, long userId) {
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        userStorage.getById(userId);
        filmStorage.addLike(filmId, userStorage.getById(userId));
        log.info("Лайк добавлен: filmId={}, userId={}", filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);
        Film film = filmStorage.getById(filmId);
        film.getLikes().remove(userStorage.getById(userId).getId());
        filmStorage.deleteLike(filmId, userStorage.getById(userId));
        log.info("Лайк удалён: filmId={}, userId={}", filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        log.debug("Запрос {} самых популярных фильмов", count);
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .toList();
    }

    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        log.debug("Поиск фильмов режиссёра с id {}, сортировка по: {}", directorId, sortBy);
        try {
            directorService.getDirectorById(directorId);
        } catch (IllegalArgumentException e) {
            log.error("Режиссёр с id {} не найден", directorId);
            throw e;
        }

        List<Film> films = filmStorage.getFilmsByDirector(directorId);

        if ("year".equals(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else {
            films.sort(Comparator.comparingInt(f -> -f.getLikes().size()));
        }

        log.debug("Найдено {} фильмов режиссёра {}", films.size(), directorId);
        return films;
    }

    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        Stream<Film> stream = filmStorage.getAll().stream();
        if (year != null && year != 0) {
            stream = stream.filter(f -> f.getReleaseDate().getYear() == year);
        }
        if (genreId != null) {
            stream = stream.filter(f -> f.getGenres().contains(genreStorage.getById(genreId)));
        }

        stream = stream.sorted(Comparator.comparingInt(f -> -f.getLikes().size()));
        if (count > 0) {
            stream = stream.limit(count);
        }
        return stream.toList();
    }

    public void deleteById(long filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new IllegalArgumentException("Фильм с id " + filmId + " не найден.");
        }
        filmStorage.deleteById(filmId);
    }
}
