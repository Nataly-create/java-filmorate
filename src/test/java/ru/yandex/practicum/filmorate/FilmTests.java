package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FilmTests {

  @Test
  public void testFilmNameEmpty() {
    Film film = Film.builder()
            .id(1)
            .description("description")
            .duration(1)
            .releaseDate(LocalDate.of(200, 1, 1))
            .build();
    assertThrows(ValidationException.class, () -> film.validate());
  }

  @Test
  public void testFilmDescriptionLong() {
    Film film = Film.builder()
            .name("Film")
            .id(1)
            .duration(1)
            .description("a".repeat(201))
            .releaseDate(LocalDate.of(200, 1, 1))
            .build();
    assertThrows(ValidationException.class, () -> film.validate());
  }

  @Test
  public void testFilmDurationNull() {
    Film film = Film.builder()
            .name("Film")
            .id(1)
            .duration(0)
            .releaseDate(LocalDate.of(200, 1, 1))
            .build();
    assertThrows(ValidationException.class, () -> film.validate());
  }

  @Test
  public void testFilmDateEarly() {
    Film film = Film.builder()
            .name("Film")
            .id(1)
            .duration(1)
            .releaseDate(LocalDate.of(1895, 12, 27))
            .build();
    assertThrows(ValidationException.class, () -> film.validate());
  }
}
