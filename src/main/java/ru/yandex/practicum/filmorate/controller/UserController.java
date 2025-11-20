package ru.yandex.practicum.filmorate.controller;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Integer, User> users = new HashMap<>();

    private int getNewId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PostMapping
    public User add(@RequestBody @NonNull User user) {
        user.validate();
        user.setId(getNewId());
        users.put(user.getId(), user);
        log.info("User {} added", user.toString());
        return user;
    }

    @PutMapping
    public User update(@RequestBody @NonNull User user) {
        user.validate();
        int id = user.getId();
        if (users.containsKey(id)) {
            users.put(id, user);
            log.info("User {} updated", user.toString());
            return user;
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException("User with id = " + id + " not found");
        }
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @DeleteMapping
    public void delete(@RequestBody @NonNull User user) {
        int id = user.getId();
        if (users.containsKey(id)) {
            log.info("User {} deleted", user.toString());
            users.remove(id);
        } else {
            log.warn("User mit id {} not found", id);
            throw new NotFoundException("User with id = " + id + " not found");
        }
    }
}
