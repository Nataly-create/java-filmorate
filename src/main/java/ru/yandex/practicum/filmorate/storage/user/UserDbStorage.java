package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.util.List;

@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    protected final JdbcTemplate jdbc;
    protected final UserRowMapper mapper;
    protected final EventRowMapper mapperEvent;
    //private final FilmDbStorage filmStorage;

    private static final String GET_ALL_QUERY = "SELECT * FROM users";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String ADD_QUERY = "INSERT INTO users(name, email, login, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, email = ?, birthday = ? WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id, confirmed) VALUES (?, ?, FALSE)";
    private static final String SET_FRIENDS_CONFIRMED_QUERY = "UPDATE friends SET confirmed = TRUE " +
            "WHERE (user_id = ? AND friend_id = ?)";
    private static final String GET_ALL_FRIENDS_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT friend_id FROM friends WHERE user_id = ?)";
    private static final String GET_COMMON_FRIENDS_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT u.friend_id FROM friends u JOIN friends f ON u.friend_id = f.friend_id " +
            "WHERE u.user_id = ? AND f.user_id= ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? and friend_id = ?";
    private static final String GET_LIKES_BY_ID_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String ADD_EVENT_QUERY = "INSERT INTO events(user_id, event_type, operation, entity_id) " +
            "VALUES (?, ?, ?, ?)";
    private static final String GET_EVENTS_QUERY = "SELECT * FROM events WHERE user_id = ?";

    @Override
    public User add(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(ADD_QUERY, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            user.setId(Long.valueOf(id));
        }
        user.validate();
        log.info("User {} added", user);
        return user;
    }

    @Override
    public User update(User user) {
        long id = user.getId();
        if (jdbc.update(UPDATE_QUERY,
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                id) > 0) {
            user.validate();
            log.info("User {} updated", user);
            return user;
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }
    }

    @Override
    public List<User> getAll() {
        return jdbc.query(GET_ALL_QUERY, mapper);
    }

    @Override
    public User getById(long id) {
        try {
            return jdbc.queryForObject(GET_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }
    }

    @Override
    public void addFriend(long id, long friendId) {
        getById(id);
        getById(friendId);
        jdbc.update(ADD_FRIEND_QUERY,
                id,
                friendId);
        if (jdbc.update(SET_FRIENDS_CONFIRMED_QUERY,
                friendId,
                id) > 0) {
            jdbc.update(SET_FRIENDS_CONFIRMED_QUERY,
                    id,
                    friendId);
        }
        log.info("Friend mit id {} added", id);
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        getById(id);
        getById(friendId);
        jdbc.update(DELETE_FRIEND_QUERY,
                id,
                friendId);
        log.info("Friend mit id {} deleted", id);
    }

    @Override
    public List<User> getFriendsById(long id) {
        getById(id);
        return jdbc.query(GET_ALL_FRIENDS_QUERY, mapper, id);
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        getById(id);
        getById(otherId);
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, mapper, id, otherId);
    }

    @Override
    public void delete(User user) {
        jdbc.update(DELETE_BY_ID_QUERY, user.getId());
        log.info("User {} deleted", user);
    }

    public List<Long> getLikesById(int id) {
        return jdbc.queryForList(GET_LIKES_BY_ID_QUERY, Long.class, id);
    }

    @Override
    public void addEvent(long userId, long entityId, EventType eventType, Operation operation) {
        getById(userId);
        jdbc.update(ADD_EVENT_QUERY, userId, eventType.toString(), operation.toString(), entityId);
    }

    @Override
    public List<Event> getEvents(long id) {
        getById(id);
        return jdbc.query(GET_EVENTS_QUERY, mapperEvent, id);
    }
}
