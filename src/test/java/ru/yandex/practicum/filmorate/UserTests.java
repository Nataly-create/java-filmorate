package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserTests {

    @Test
    public void testEmailEmpty() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder()
                .id(1)
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .login("login")
                .build();
        assertThrows(ValidationException.class, () -> user.validate());
    }

    @Test
    public void testEmailWithoutAt() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder()
                .id(1)
                .name("name")
                .email("email")
                .birthday(LocalDate.of(2000, 1, 1))
                .login("login")
                .build();
        assertThrows(ValidationException.class, () -> user.validate());
    }

    @Test
    public void testBirthdayTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder()
                .id(1)
                .name("User")
                .login("login")
                .email("email@email")
                .birthday(tomorrow)
                .build();
        assertThrows(ValidationException.class, () -> user.validate());
    }

    @Test
    public void testLoginSpace() {
        User user = User.builder()
                .id(1)
                .login(" ")
                .email("email@email")
                .birthday(LocalDate.of(200, 1, 1))
                .build();
        assertThrows(ValidationException.class, () -> user.validate());
    }
}
