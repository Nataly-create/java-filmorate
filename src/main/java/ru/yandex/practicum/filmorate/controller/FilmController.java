package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Validated
@Getter
@Setter
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film add(@RequestBody @Valid Film film) {
        return filmService.add(film);
    }

    @PutMapping
    public Film update(@RequestBody @NonNull Film film) {
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable long id) {
        return filmService.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(required = false, defaultValue = "0") Integer count,
                                          @RequestParam(required = false) Integer genreId,
                                          @RequestParam(required = false, defaultValue = "0") Integer year) {
        if (genreId == null && year == 0) {
            return filmService.getMostPopularFilms(count == 0 ? 10 : count);
        }
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam long userId, @RequestParam long friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Пользователи должны быть разными");
        }

        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @DeleteMapping
    public void delete(@RequestBody @NonNull Film film) {
        filmService.delete(film);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Positive long id) {
        try {
            filmService.deleteById(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable long directorId,
            @RequestParam(defaultValue = "likes") String sortBy) {

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new IllegalArgumentException("Параметр sortBy должен быть 'year' или 'likes'");
        }

        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam String query,
            @RequestParam String by) {
        if (query.isBlank()) {
            throw new IllegalArgumentException("Параметр query не должен быть пустым");
        }

        List<String> searchFields = by != null
                ? Arrays.asList(by.split(","))
                : List.of("title");

        if (!Set.of("director", "title").containsAll(searchFields)) {
            throw new IllegalArgumentException("Параметр by должен быть 'director' или 'title'");
        }

        return filmService.searchFilms(query, searchFields);
    }
}
