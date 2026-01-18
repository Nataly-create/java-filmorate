package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Film {
    private static final Logger log = LoggerFactory.getLogger(Film.class);
    private long id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Min(1)
    private long duration;
    private Mpa mpa;
    @Builder.Default
    private HashSet<Genre> genres = new HashSet<>();
    @Builder.Default
    private HashSet<Long> likes = new HashSet<>();
    @Builder.Default
    private Set<Director> directors = new HashSet<>();

    public Set<Long> getLikes() {
        if (likes == null) {
            likes = new HashSet<>();
        }
        return likes;
    }

    public Set<Genre> getGenres() {
        if (genres == null) {
            genres = new HashSet<>();
        }
        return genres;
    }

    public Set<Director> getDirectors() {
        if (directors == null) {
            directors = new HashSet<>();
        }
        return directors;
    }

    public void validate() {
        StringBuilder message = new StringBuilder();
        if (getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            message.append("The release date must be on or after December 28, 1895\n");
        }
        if (!message.isEmpty()) {
            String errorMessage = message.toString().trim();
            log.warn("Film {}.\n{}", this, errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
