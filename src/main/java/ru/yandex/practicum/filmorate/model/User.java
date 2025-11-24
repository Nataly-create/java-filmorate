package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import java.time.LocalDate;

@Data
@Builder
public class User {
  private static final Logger log = LoggerFactory.getLogger(User.class);
  private Integer id;
  @Email
  @NotNull
  private String email;
  @NotNull
  @NotBlank
  private final String login;
  private String name;
  @Past
  private LocalDate birthday;

  public void validate() {
    StringBuilder message = new StringBuilder();

    if (!message.isEmpty()) {
      String errorMessage = message.toString().trim();
      log.warn("User {}.\n{}", this.toString(), errorMessage);
      throw new ValidationException(errorMessage);
    }
  }

  public String getName() {
    if (name == null || name.isBlank()) {
      return login;
    }
    return name;
  }
}
