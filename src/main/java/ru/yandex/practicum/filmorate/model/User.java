package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
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
public class User {
  private static final Logger log = LoggerFactory.getLogger(User.class);
  private long id;
  @Email
  @NotEmpty
  private String email;
  @NotBlank
  private final String login;
  private String name;
  @Past
  private LocalDate birthday;
  @Builder.Default
  private HashSet<Long> friends = new HashSet<>();

  public void validate() {
    StringBuilder message = new StringBuilder();

    if (!message.isEmpty()) {
      String errorMessage = message.toString().trim();
      log.warn("User {}.\n{}", this, errorMessage);
      throw new ValidationException(errorMessage);
    }
  }

  public String getName() {
    if (name == null || name.isBlank()) {
      return login;
    }
    return name;
  }

  public Set<Long> getFriends() {
    if (friends == null) {
      friends = new HashSet<>();
    }
    return friends;
  }
}
