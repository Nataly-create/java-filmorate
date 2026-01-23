package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.film.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final DirectorDbStorage directorStorage;
    private final DirectorService directorService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreStorage genreStorage,
                       DirectorService directorService,
                       DirectorDbStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.directorService = directorService;
        this.directorStorage = directorStorage;
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

    public void addLike(long id, long userId) {
        filmStorage.addLike(id, userStorage.getById(userId));
        userStorage.addEvent(userId, id, EventType.LIKE, Operation.ADD);
    }

    public void deleteLike(long id, long userId) {
        filmStorage.deleteLike(id, userStorage.getById(userId));
        userStorage.addEvent(userId, id, EventType.LIKE, Operation.REMOVE);
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

    public List<Film> getCommonFilms(long userId, long friendId) {
        log.debug("Поиск общих фильмов у пользователя {} и друга {}", userId, friendId);

        userStorage.getById(userId);
        userStorage.getById(friendId);

        if (userStorage.getFriendsById(userId) == null || userStorage.getFriendsById(friendId) == null) {
            throw new IllegalArgumentException("Пользователи не являются друзьями");
        }

        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);

        log.debug("Найдено {} общих фильмов у двух друзей", commonFilms.size());
        return commonFilms;
    }

    public void deleteById(long filmId) {
        if (!filmStorage.existsById(filmId)) {
            throw new IllegalArgumentException("Фильм с id " + filmId + " не найден.");
        }
        filmStorage.deleteById(filmId);
    }

    public Collection<Film> searchFilms(String query, List<String> by) {
        log.debug("Запрос на поиск фильмов содержащих: {}, в {}.", query, by);

        Collection<Film> foundFilms;

        if (by.size() > 1) {
            foundFilms = Stream.concat(
                    directorStorage.getFilmsIdByDirector(query).stream()
                            .map(filmStorage::getById)
                            .filter(Objects::nonNull),
                    filmStorage.searchFilms(query).stream()
            ).distinct().collect(Collectors.toList());
        } else {
            foundFilms = by.getFirst().equals("director")
                    ? directorStorage.getFilmsIdByDirector(query).stream()
                    .map(filmStorage::getById)
                    .collect(Collectors.toList())
                    : filmStorage.searchFilms(query);
        }

        foundFilms = foundFilms.stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed())
                .collect(Collectors.toList());

        log.debug("Найдено {} фильмов", foundFilms.size());
        return foundFilms;
    }
}
