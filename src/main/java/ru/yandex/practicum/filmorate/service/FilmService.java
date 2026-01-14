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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorService directorService;
    public final FilmStorage filmStorage;
    public final UserStorage userStorage;
    public final GenreStorage genreStorage;

    FilmService(@Autowired @Qualifier("filmDbStorage") FilmStorage filmStorage,
                @Autowired @Qualifier("userDbStorage") UserStorage userStorage,
                @Autowired @Qualifier("genreDbStorage") GenreStorage genreStorage) {
    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
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

    public List<Film> getMostPopularFilms(Optional<Integer> count, Optional<Integer> genreId, Optional<Integer> year) {
        if (genreId.isEmpty() && year.isEmpty()) {
            return getMostPopularFilms(count.orElse(10));
        }

        Stream<Film> stream = filmStorage.getAll().stream();
        if (year.isPresent()) {
            stream = stream.filter(f -> f.getReleaseDate().getYear() == year.get());
        }
        if (genreId.isPresent()) {
            stream = stream.filter(f -> f.getGenres().contains(genreStorage.getById(genreId.get())));
        }

        stream = stream.sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
        if (count.isPresent()) {
            stream = stream.limit(count.get());
        }
        return stream.toList();
    }
}
