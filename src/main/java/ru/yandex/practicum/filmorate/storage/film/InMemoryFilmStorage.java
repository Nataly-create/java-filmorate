package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        film.setId(getNewId());
        films.put(film.getId(), film);
        log.info("Film {} added", film.toString());
        return film;
    }

    @Override
    public Film update(Film film) {
        long id = film.getId();
        if (films.containsKey(id)) {
            films.put(id, film);
            log.info("Film {} updated", film.toString());
            return film;
        } else {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
    }

    @Override
    public Film getById(long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void addLike(long id, User user) {
        if (!films.containsKey(id)) {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
        films.get(id).getLikes().add(user.getId());
    }

    @Override
    public void deleteLike(long id, User user) {
        if (!films.containsKey(id)) {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
        films.get(id).getLikes().remove(user.getId());
    }

    @Override
    public void delete(Film film) {
        long id = film.getId();
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException(id, "Film");
        }
    }

    @Override
    public Map<Long, Set<Long>> getAllLikes() {
        return new HashMap<>();
    }

    private long getNewId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public void deleteById(long id) {
        if (!films.containsKey(id)) {
            throw new IllegalArgumentException("Фильм с id " + id + " не найден.");
        }
        films.remove(id);
    }

    @Override
    public boolean existsById(long id) {
        return films.containsKey(id);
    }

    @Override
    public List<Film> getFilmsByDirector(long directorId) {
        return films.values().stream()
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(director -> director.getId() == directorId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> searchFilms(String query) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        return List.of();
    }
}
