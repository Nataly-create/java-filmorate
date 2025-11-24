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

@Data
@Builder
public class Film {
    private static final Logger log = LoggerFactory.getLogger(Film.class);
    private int id;
    @NotBlank
    @NotNull
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Min(1)
    private long duration;

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
