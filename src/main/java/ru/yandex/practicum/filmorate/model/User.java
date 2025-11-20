package ru.yandex.practicum.filmorate.model;

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
  @Builder.Default
  private String email = "";
  @Builder.Default
  private final String login = "";
  private String name;
  private LocalDate birthday;

  public void validate() {
    StringBuilder message = new StringBuilder();
    if (getEmail().isEmpty()) {
      message.append("Email is required\n");
    }
    if (!getEmail().contains("@")) {
      message.append("Email must contain an @ symbol\n");
    }
    if (getLogin().isEmpty()) {
      message.append("Login is required\n");
    }
    if (getLogin().contains(" ")) {
      message.append("Login must not contain space\n");
    }
    if (getBirthday() != null && getBirthday().isAfter(LocalDate.now())) {
      message.append("Birthday must be earlier than today");
    }
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
