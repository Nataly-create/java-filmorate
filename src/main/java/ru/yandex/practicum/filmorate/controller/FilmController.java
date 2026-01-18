package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

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

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @DeleteMapping
    public void delete(@RequestBody @NonNull Film film) {
        filmService.delete(film);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @Positive long id) {
        filmService.deleteById(id);
    }
}
