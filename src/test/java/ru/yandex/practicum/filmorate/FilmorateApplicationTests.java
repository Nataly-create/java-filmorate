package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class FilmorateApplicationTests {
	@Autowired
	private UserDbStorage userStorage;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void addUser() {
		User user = User
				.builder()
				.email("test@test.com")
				.name("name")
				.birthday(LocalDate.of(2000, 01, 01))
				.login("login")
						.build();
		userStorage.add(user);
	}

	@Test
	public void testFindUserById() {

		User userNew = User
				.builder()
				.email("test@test.com")
				.name("name")
				.birthday(LocalDate.of(2000, 01, 01))
				.login("login")
				.build();
		long id = userStorage.add(userNew).getId();

		Optional<User> userOptional = Optional.of(userStorage.getById(id));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	public void testUpdateUser() {
		List<User> users = userStorage.getAll();

		assertThat(users).isNotEmpty();

		long id = users.getFirst().getId();
		User user = userStorage.getById(id);
		user.setName("Update");
		userStorage.update(user);
		User userFromDb = userStorage.getById(id);
		assertThat(userFromDb.getName()).isEqualTo("Update");
	}
}