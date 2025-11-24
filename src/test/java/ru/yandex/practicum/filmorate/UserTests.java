package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class UserTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testEmailEmpty() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder()
                .id(1)
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .login("login")
                .build();
        Set<ConstraintViolation<User>> res = validator.validate(user);
        assertFalse(res.isEmpty(), "Validation should fail for a empty email");
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
        Set<ConstraintViolation<User>> res = validator.validate(user);
        assertFalse(res.isEmpty(), "Validation should fail for a email without at");
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
        Set<ConstraintViolation<User>> res = validator.validate(user);
        assertFalse(res.isEmpty(), "Validation should fail for a birthday tomorrow");
    }

    @Test
    public void testLoginSpace() {
        User user = User.builder()
                .id(1)
                .login(" ")
                .email("email@email")
                .birthday(LocalDate.of(200, 1, 1))
                .build();
        Set<ConstraintViolation<User>> res = validator.validate(user);
        assertFalse(res.isEmpty(), "Validation should fail for a spase in login");
    }
}
