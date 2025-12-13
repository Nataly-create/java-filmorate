package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        user.setId(getNewId());
        users.put(user.getId(), user);
        log.info("User {} added", user);
        return user;
    }

    @Override
    public User update(User user) {
        long id = user.getId();
        if (users.containsKey(id)) {
            users.put(id, user);
            log.info("User {} updated", user);
            return user;
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }
    }

    @Override
    public List<User> getFriendsById(long id) {
        if (users.containsKey(id)) {
            return getById(id).getFriends()
                    .stream()
                    .map(users::get)
                    .collect(Collectors.toList());
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }
    }

    @Override
    public List<User> getCommonFriends(long id, long otherId) {
        if (!users.containsKey(id)) {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }

        if (!users.containsKey(otherId)) {
            log.warn("User mit id {} not found", otherId);
            throw new NotFoundException(otherId, "User");
        }
        Set<Long> otherFriends = users.get(otherId).getFriends();
        return getById(id).getFriends()
                .stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .collect(Collectors.toList());

    }

    @Override
    public void delete(User user) {
        long id = user.getId();
        if (users.containsKey(id)) {
            log.info("User {} deleted", user.toString());
            users.remove(id);
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException(id, "User");
        }
    }

    private long getNewId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
