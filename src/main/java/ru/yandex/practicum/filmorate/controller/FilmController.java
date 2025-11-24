package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Integer, Film> films = new HashMap<>();

    private int getNewId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public Film add(@RequestBody @Valid Film film) {
        film.validate();
        film.setId(getNewId());
        films.put(film.getId(), film);
        log.info("Film {} added", film.toString());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody @NonNull Film film) {
        film.validate();
        int id = film.getId();
        if (films.containsKey(id)) {
            films.put(id, film);
            log.info("Film {} updated", film.toString());
            return film;
        } else {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException("Film with id = " + id + " not found");
        }
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @DeleteMapping
    public void delete(@RequestBody @NonNull Film film) {
        int id = film.getId();
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            log.warn("Film with id {} not found", id);
            throw new NotFoundException("Film with id = " + id + " not found");
        }
    }
}
