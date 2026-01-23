package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable @Positive Long id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director create(@RequestBody @Valid Director director) {
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director update(@RequestBody @Valid Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive Long id) {
        directorService.deleteDirector(id);
    }
}
