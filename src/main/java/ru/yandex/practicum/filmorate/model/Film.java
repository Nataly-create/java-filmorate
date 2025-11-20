package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    private static final Logger log = LoggerFactory.getLogger(Film.class);
    private int id;
    @Builder.Default
    private String name = "";
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public void validate() {
        StringBuilder message = new StringBuilder();
        if (getName().isEmpty()) {
            message.append("Name is required\n");
        }
        if (getDescription() != null && getDescription().length() > 200) {
            message.append("Description must be 200 characters or less\n");
        }
        if (getDuration() <= 0) {
            message.append("Duration must be positive\n");
        }
        if (getReleaseDate() != null && getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            message.append("The release date must be on or after December 28, 1895\n");
        }
        if (!message.isEmpty()) {
            String errorMessage = message.toString().trim();
            log.warn("Film {}.\n{}", this, errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
